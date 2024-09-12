module org.example.demo3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires jsch;
    requires com.fasterxml.jackson.databind;

    opens com.harveyvo.java.tunnel to javafx.fxml;
    exports com.harveyvo.java.tunnel;
}