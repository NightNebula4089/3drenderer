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

        for(int i = 0; i < tris.size(); i++){
            Triangle t = tris.get(i);
            Path path = new Path();
            MoveTo moveto = new MoveTo();
            moveto.setX(t.v1.x+300);
            moveto.setY(t.v1.y+300);
            LineTo lineto1 = new LineTo();
            lineto1.setX(t.v2.x+300);
            lineto1.setY(t.v2.y+300);
            LineTo lineto2 = new LineTo();
            lineto2.setX(t.v3.x+300);
            lineto2.setY(t.v3.y+300);
            LineTo lineto3 = new LineTo();
            lineto3.setX(t.v1.x+300);
            lineto3.setY(t.v1.y+300);
            path.getElements().add(moveto);
            path.getElements().add(lineto1);
            path.getElements().add(lineto2);
            path.getElements().add(lineto3);
            root.getChildren().add(path);
        }



        Scene scene = new Scene(root, 600, 600, Color.WHITE);
        Stage primaryStage = new Stage();

        scroll.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                root.getChildren().remove(5);
                root.getChildren().remove(4);
                root.getChildren().remove(3);
                root.getChildren().remove(2);
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
                    Path path = new Path();
                    MoveTo moveto = new MoveTo();
                    moveto.setX(t.v1.x+300);
                    moveto.setY(t.v1.y+300);
                    LineTo lineto1 = new LineTo();
                    lineto1.setX(t.v2.x+300);
                    lineto1.setY(t.v2.y+300);
                    LineTo lineto2 = new LineTo();
                    lineto2.setX(t.v3.x+300);
                    lineto2.setY(t.v3.y+300);
                    LineTo lineto3 = new LineTo();
                    lineto3.setX(t.v1.x+300);
                    lineto3.setY(t.v1.y+300);
                    path.getElements().add(moveto);
                    path.getElements().add(lineto1);
                    path.getElements().add(lineto2);
                    path.getElements().add(lineto3);
                    root.getChildren().add(path);
                }
            }
        });

        scroll2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                root.getChildren().remove(5);
                root.getChildren().remove(4);
                root.getChildren().remove(3);
                root.getChildren().remove(2);

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
                    Path path = new Path();
                    MoveTo moveto = new MoveTo();
                    moveto.setX(t.v1.x+300);
                    moveto.setY(t.v1.y+300);
                    LineTo lineto1 = new LineTo();
                    lineto1.setX(t.v2.x+300);
                    lineto1.setY(t.v2.y+300);
                    LineTo lineto2 = new LineTo();
                    lineto2.setX(t.v3.x+300);
                    lineto2.setY(t.v3.y+300);
                    LineTo lineto3 = new LineTo();
                    lineto3.setX(t.v1.x+300);
                    lineto3.setY(t.v1.y+300);
                    path.getElements().add(moveto);
                    path.getElements().add(lineto1);
                    path.getElements().add(lineto2);
                    path.getElements().add(lineto3);
                    root.getChildren().add(path);
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
}
