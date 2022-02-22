/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appayuda;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.web.*;
import javafx.util.Callback;
import netscape.javascript.JSObject;

/**
 *
 * @author jose
 */
public class AppAyuda extends Application {
    
    // Instanciamos la escena de forma global a la clase, para acceder a ella desde cualquier metodo
    private Scene scene;
    
    @Override
    public void start(Stage stage) {
        // Establecemos titulo de la escena
        stage.setTitle("AppAyuda");
        // Creamos la escena con una instancia de Browser como nodo raiz y dandole color
        scene = new Scene(new Browser(),750,500, Color.web("#666970"));
        // Establecemos la escena como la escena principal del escenario
        stage.setScene(scene);
        // Aplicamos estilo
//      scene.getStylesheets().add(App.class.getResource("BrowserToolbar.css").toExternalForm());
        // Mostramos el escenario
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

class Browser extends Region {
    
    // HBox para el toolbar
    private HBox toolBar;
    // Array con los paths a las imagenes
    private static String[] imageFiles = new String[]{
        "/resources/images/moodle.jpg",
        "/resources/images/facebook.jpg",
        "/resources/images/documentation.png",
        "/resources/images/twitter.jpg",
        "/resources/images/help.png" // Ayuda
    };
    // Array de captions, 
    private static String[] captions = new String[]{
        "Moodle",
        "FaceBook",
        "Documentation",
        "Twitter",
        "Help" // Ayuda
    };
    // Array de URLs empleadas
    private static String[] urls = new String[]{
        "http://aula.ieslosmontecillos.es/",
        "https://es-es.facebook.com/ieslosmontecillos",
        AppAyuda.class.getResource("/resources/html/TopicGeneral.html").toExternalForm(), //"http://docs.oracle.com/javase/index.html",
        "https://twitter.com/losmontecillos?lang=es",
        AppAyuda.class.getResource("/resources/html/help.html").toExternalForm() // Añadimos pagina de ayuda
    };
    // Visor con la imagen seleccionada
    final ImageView selectedImage = new ImageView();
    // Array de hipervinculos (las rutas de los captions)
    final Hyperlink[] hpls = new Hyperlink[captions.length];
    // Array para las imagenes
    final Image[] images = new Image[imageFiles.length];
    // Variable de control para saber si se muestra el topic Help o no
    private boolean needDocumentationButton = false;
    // Variable con el visor web
    final WebView browser = new WebView();
    // Variable con el motor web
    final WebEngine webEngine = browser.getEngine();
    
    // Boton toggle para hacer visible el topic Help
    final Button toggleHelpTopics = new Button("Toggle Help Topics");
    // El boton de needDocumentationButton está arriba
    
    // WebView para las ventanas emergentes (Hacer click derecho en un componente de la pagina, el desplegable)
    final WebView smallView = new WebView();
    
    // Variable para mostrar el historial de busqueda
    final ComboBox comboBox = new ComboBox();
    
    // Constructor de la clase Browser
    public Browser() {
        // Aplicamos estilo
        getStyleClass().add("browser");
        // Tratamos los enlaces, los linkamos con las imagenes para que al pulsar en ellas nos redirijan a la pagina correspondiente
        for(int i = 0; i < captions.length; i++) {
            // Creamos un hipervinculo a traves de los captions del array, almacenandolos en el array de hipervinculos y en la variable que vamos a utilizar
            Hyperlink hpl = hpls[i] = new Hyperlink(captions[i]);
            // Hacemos lo mismo para las imagenes
            Image image = images[i] = new Image(getClass().getResourceAsStream(imageFiles[i]));
            // Vinculamos el hipervinculo con la imagen que corresponde a dicho hipervinculo
            hpl.setGraphic(new ImageView(image));
            // Almacenamos la URL que corresponde con el caption
            final String url = urls[i];
            
            // Se comprueba si es necesario crear el boton toggle (solo cuando la pagina sea la de Help (la de ayuda)
            final boolean addButton = (hpl.getText().equals("Help"));
            
            // Establecemos el manejador del evento de accion por defecto del hipervinculo (hacer click en la imagen con el hipervinculo)
            hpl.setOnAction(new EventHandler<ActionEvent>() {
                // Sobreescribimos el metodo de la interfaz EventHandler
                @Override
                public void handle(ActionEvent event) {
                    // Seteamos la variable que indica si se debe mostrar o no la documentacion (depende de si el topic es Help o no)
                    needDocumentationButton = addButton;
                    // Cargamos la pagina del hipervinculo en el motor web del visor
                    webEngine.load(url);
                }
            });
        }
        
        // Establecemos el ancho preferido del combo box que muestra el historial
        comboBox.setPrefWidth(60);
        
        // Establecemos la pagina inicial que se verá en el visor
        webEngine.load("http://www.ieslosmontecillos.es");
        
        // Creamos el ToolBar
        toolBar = new HBox();
        // Alineamos al centro
        toolBar.setAlignment(Pos.CENTER);
        // Añadimos estilo
        toolBar.getStyleClass().add("browser-toolbar");
        // Añadimos el ComboBox que muestra el historial al toolbar (lo añadimos el primero para que aparezca el primero (a la izquierda)
        toolBar.getChildren().add(comboBox);
        // Añadimos los hipervinculos
        toolBar.getChildren().addAll(hpls);
        // Añadimos espacio entre los componentes (se crea en metodo)
        toolBar.getChildren().add(createSpacer());
        
        // Establecemos el manejador de eventos del boton toggle
        toggleHelpTopics.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                // Ejecutamos JavaScript
                webEngine.executeScript("toggle_visibility('help_topics')");
            }
        });
        
        // Gestion de ventanas emergentes
        // Establecemos tamaño del visor
        smallView.setPrefSize(120, 80);
        // Manejador de las ventanas emergentes (los enlaces del toolbar)
        webEngine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine> (){
            @Override
            public WebEngine call(PopupFeatures config) {
                // Establecemos la escala de la fuente del visor
                smallView.setFontScale(0.8);
                // Si el toolbar no contiene el visor, se le añade
                if(!toolBar.getChildren().contains(smallView)){
                    toolBar.getChildren().add(smallView);
                }
                
                // Devolvemos el motor del navegador de la ventana emergente
                return smallView.getEngine();
            }
            
        });
        
        // Procesamiento del historial de navegación
        // Variable con el historial del motor
        final WebHistory history = webEngine.getHistory();
        // Listener para las entradas que se añadan en el historial. getEntries nos devuelve las paginas visitadas como una lista observable
        // por ello podemos añadirle un ListChangeListener
        history.getEntries().addListener((ListChangeListener.Change<? extends WebHistory.Entry> c) -> {
            // Tomamos la siguiente entrada
            c.next();
            // Tomamos las eliminadas, y las eliminamos del comboBox
            c.getRemoved().stream().forEach((e) -> {
                comboBox.getItems().remove(e.getUrl());
            });
            // Tomamos las añadidas (visitadas), y añadimos dichas paginas al ComboBox con el historial
            c.getAddedSubList().stream().forEach((e) -> {
                comboBox.getItems().add(e.getUrl());
            });
        });
        
        // Establecemos el comportamiento del ComboBox
        comboBox.setOnAction((Event ev) -> {
            // Calculamos la cantidad de veces que tenemos que ir hacia atras en la navegacion
            // indice seleccionado de la lista del comboBox - el indice en el que nos encontramos actualmente
            // Debemos tener en cuenta que el historial de navegacion se almacena en fomra de lista a la cual se le van añadiendo las paginas visitadas
            int offset = comboBox.getSelectionModel().getSelectedIndex() - history.getCurrentIndex();
            // Cargamos la pagina a la que queremos dirigirnos del historial
            history.go(offset);
        });
        
        // Creamos escuchador que, cuando el visor termine de cargar la pagina que deseamos ver
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                // Eliminamos el toggle button del toolbar
                toolBar.getChildren().remove(toggleHelpTopics);
                
                // Si el estado es SUCCEEDED es que se ha terminado de cargar la pagina
                if(newState == State.SUCCEEDED) {
                    // Creamos un JObject que ejecuta la funcion JavaScript window, la cual sirve para hacer visible metodos de JavaFX desde JavaScript en la pagina cargada
                    JSObject win = (JSObject) webEngine.executeScript("window");
                    // Agregamos la clase JavaApp como miembro del objeto que representa la ventana creado anteriormente, bajo el alias app (este alias servirá para llamar al metodo desde el HTML)
                    win.setMember("app", new JavaApp());
                    // Si se necesita el boton (si la pagina cargada es help)
                    if(needDocumentationButton) {
                        toolBar.getChildren().add(toggleHelpTopics);
                    }
                }
            }
            
        });
        
        // Añadimos componentes a la region (toolbar y visor)
        getChildren().add(toolBar);
        getChildren().add(browser);
    }
    
    // Clase que representa una interfaz para llamarla desde JavaScript
    public class JavaApp {
        // Metodo para salir de la App
        public void exit() {
            Platform.exit();
        }
    }
    // Metodo JavaFX que llamamos a traves de un comando JavaScript desde el visor web
    
    
    // Metodo para crear el espaciador
    private Node createSpacer() {
        
        // Creamos una region
        Region spacer = new Region();
        // Hacemos que el espaciador crezca horizontalmente siempre que se de la opcion
        HBox.setHgrow(spacer, Priority.ALWAYS);
        // Devolvemos el espaciador
        return spacer;
        
    }
    
    // Metodo sobreescrito que establece el layout de los hijos de la region
    @Override
    protected void layoutChildren() {
        // Extraemos el ancho de la region
        double w = getWidth();
        // Extraemos el alto de la region
        double h = getHeight();
        // 
        double tbHeight = toolBar.prefHeight(w);
        // Modficamos el layout para el visor
        layoutInArea(browser, 0, 0, w, h-tbHeight, 0, HPos.CENTER, VPos.CENTER);
        // Modificamos layout para el toolBar
        layoutInArea(toolBar, 0, h-tbHeight, w, tbHeight, 0, HPos.CENTER, VPos.CENTER);
    }
    
    // Metodo sobreescrito que devuelve el ancho preferido de la region
    @Override
    protected double computePrefWidth(double height) {
        return 750;
    }
    // Metodo sobreescrito que devuelve el alto preferido de la region
    @Override
    protected double computePrefHeight(double width) {
        return 500;
    }
}
