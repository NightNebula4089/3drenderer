package renderer;

import com.sun.javafx.property.adapter.PropertyDescriptor;
import javafx.animation.PathTransition;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.awt.geom.Path2D;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ScrollBar;


public class Main extends Application {

        public class Matrix3 {
            double[] values;

            public Matrix3(double[] values){
                this.values = values;
            }

            public Matrix3 multiplyMatrix(Matrix3 other){
                double[] result = new double[9];
                for (int row = 0; row < 3 ; row++){
                    for (int col = 0 ; col < 3 ; col++){
                        for (int i =0 ; i < 3 ; i++){
                            result[row*3 + col] += this.values[row*3 + i]*other.values[i*3 + col];
                        }
                    }
                }

                return new Matrix3(result);
            }

            public Vertex transform(Vertex input){
                return new Vertex(
                        input.x * values[0] + input.y * values[3] + input.z * values[6],
                        input.x * values[1] + input.y * values[4] + input.z * values[7],
                        input.x * values[2] + input.y * values[5] + input.z * values[8]
                );
            }
        }

        public class Vertex{
            double x;
            double y;
            double z;
            public Vertex(double x, double y, double z){
                this.x = x;
                this.y = y;
                this.z = z;
            }
        }

        public class Triangle{
            Vertex v1;
            Vertex v2;
            Vertex v3;
            Color color;

            public Triangle(Vertex v1, Vertex v2 , Vertex v3 , Color color){
                this.v1 = v1;
                this.v2 = v2;
                this.v3 = v3;
                this.color = color;
            }

            public Vertex getVertex(int i){
                switch(i)
                {
                    case 1 : return this.v1;
                    case 2 : return this.v2;
                    case 3 : return this.v3;
                }
                return v1;
            }

        }


    @Override
    public void start(Stage stage) throws Exception {

        ArrayList<Triangle> tris = new ArrayList<Triangle>();
        tris.add(new Triangle(new Vertex (100.0,100.0,100.0),
                new Vertex(-100.0,-100.0,100.0),
                new Vertex(-100.0,100.0,-100.0) , Color.YELLOW));

        tris.add(new Triangle(new Vertex (100.0,100.0,100.0),
                new Vertex(-100.0,-100.0,100.0),
                new Vertex(100.0,-100.0,-100.0) , Color.RED));

        tris.add(new Triangle(new Vertex (-100.0,100.0,-100.0),
                new Vertex(100.0,-100.0,-100.0),
                new Vertex(100.0,100.0,100.0) , Color.GREEN));

        tris.add(new Triangle(new Vertex (-100.0,100.0,-100.0),
                new Vertex(100.0,-100.0,-100.0),
                new Vertex(-100.0,-100.0,100.0) , Color.BLUE));

        Slider scroll = new Slider(0, 10, 0);
        scroll.resizeRelocate(250.0,500.0,2.0,2.0);
        Group root = new Group();
        root.getChildren().add(scroll);

        Slider scroll2 = new Slider(0,10,0);
        scroll2.relocate(500,300);
        scroll2.setOrientation(Orientation.VERTICAL);
        root.getChildren().add(scroll2);

        Scene scene = new Scene(root, 600, 600, Color.WHITE);
        Stage primaryStage = new Stage();

        double [][] z_buffer = new double[(int)scene.getWidth()][(int)scene.getHeight()];
        for(int i = 0; i < (int)scene.getWidth(); i++){
            for(int j = 0; j < (int)scene.getHeight(); j++){
                z_buffer[i][j] = Double.NEGATIVE_INFINITY;
            }
        }

        ImageView renderedimg = new ImageView();
        WritableImage image = new WritableImage((int) scene.getHeight(), (int) scene.getWidth());

        for(Triangle trig : tris){
            drawTriangle(trig,image,z_buffer);
        }

        renderedimg.setImage(image);
        root.getChildren().add(renderedimg);

        scroll.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                eraseImage(image);
                for(int i = 0; i < (int)scene.getWidth(); i++){
                    for(int j = 0; j < (int)scene.getHeight(); j++){
                        z_buffer[i][j] = Double.NEGATIVE_INFINITY;
                    }
                }
                double angle = Math.toRadians(scroll.getValue());
                Matrix3 transform = new Matrix3(new double[] {
                        Math.cos(angle),0,-Math.sin(angle),
                        0,1,0,
                        Math.sin(angle),0,Math.cos(angle)
                });

                for(int i = 0; i < tris.size(); i++){
                    Triangle t = tris.get(i);
                    t.v1 = transform.transform(t.v1);
                    t.v2 = transform.transform(t.v2);
                    t.v3 = transform.transform(t.v3);
                    drawTriangle(t,image,z_buffer);
                }
            }
        });

        scroll2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                eraseImage(image);
                for(int i = 0; i < (int)scene.getWidth(); i++){
                    for(int j = 0; j < (int)scene.getHeight(); j++){
                        z_buffer[i][j] = Double.NEGATIVE_INFINITY;
                    }
                }
                double angle = Math.toRadians(scroll2.getValue());

                Matrix3 transform = new Matrix3(new double[] {
                        1,0,0,
                        0,Math.cos(angle),Math.sin(angle),
                        0,-Math.sin(angle),Math.cos(angle)
                });

                for(int i = 0; i < tris.size(); i++){
                    Triangle t = tris.get(i);
                    t.v1 = transform.transform(t.v1);
                    t.v2 = transform.transform(t.v2);
                    t.v3 = transform.transform(t.v3);
                    drawTriangle(t,image,z_buffer);
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static boolean isIn(Triangle trig, int x, int y,double[][] z_buffer){
        // v1(A) v2(B) v3(C)
        // PBC = det(v2 - (x,y); v3 - (x,y))
        // APC = det((x,y) - v1; v3 - v1)
        // ABP = det (v2 - v1; (x,y) - v1)
        // ABC = det(v3 - v1; v2 - v1)
        double trigarea = (trig.v2.x - trig.v1.x)*(trig.v3.y - trig.v1.y) - (trig.v2.y-trig.v1.y)*(trig.v3.x - trig.v1.x);
        double b1 = ((trig.v2.x - x)*(trig.v3.y - y) - (trig.v2.y - y)*(trig.v3.x - x))/(trigarea);
        double b2 = ((x - trig.v1.x)*(trig.v3.y - trig.v1.y) - (y - trig.v1.y)*(trig.v3.x - trig.v1.x))/(trigarea);
        double b3 = ((trig.v2.x - trig.v1.x)*(y - trig.v1.y) - (trig.v2.y - trig.v1.y)*(x - trig.v1.x))/(trigarea);
        if (b1 <= 1 && b1 >= 0 && b2 <= 1 && b2 >= 0 && b3 <= 1 && b3 >= 0){
            if(z_buffer[x][y] < b1*trig.v1.z + b2*trig.v2.z + b3*trig.v3.z){
                z_buffer[x][y] = b1*trig.v1.z + b2*trig.v2.z + b3*trig.v3.z;
                return true;
            } else return false;
        } else {
            return false;
        }
    }

    public void drawTriangle(Triangle trig, WritableImage image, double[][] z_buffer){
        Vertex v1 = trig.v1;
        Vertex v2 = trig.v2;
        Vertex v3 = trig.v3;
        PixelWriter writer = image.getPixelWriter();

        double x1 = v1.x + image.getWidth()/2;
        double y1 = v1.y + image.getHeight()/2;
        double x2 = v2.x + image.getWidth()/2;
        double y2 = v2.y + image.getHeight()/2;
        double x3 = v3.x + image.getWidth()/2;
        double y3 = v3.y + image.getHeight()/2;

        Triangle newtrig = new Triangle(new Vertex(x1,y1,trig.v1.z),new Vertex(x2,y2,trig.v2.z),new Vertex(x3,y3,trig.v3.z),trig.color);

        int minX = (int) Math.max(0,Math.ceil(Math.min(x1,Math.min(x2,x3))));
        int maxX = (int) Math.min(image.getWidth()-1,Math.floor(Math.max(x1,Math.max(x2,x3))));

        int minY = (int) Math.max(0,Math.ceil(Math.min(y1,Math.min(y2,y3))));
        int maxY = (int) Math.min(image.getHeight()-1,Math.floor(Math.max(y1,Math.max(y2,y3))));


        for(int x = minX; x <= maxX; x++){
            for(int y = minY; y <= maxY; y++){
                if(isIn(newtrig,x,y,z_buffer)){
                    writer.setColor(x, y, trig.color);
                }
            }
        }
    }

    public static void eraseImage(WritableImage image){
        PixelWriter writer = image.getPixelWriter();
        for(int i = 0; i < image.getWidth(); i++){
            for(int j = 0; j < image.getHeight(); j++){
                writer.setColor(i,j,Color.TRANSPARENT);
            }
        }
    }

    public static void main(String[] args){
        launch(args);
    }
}
