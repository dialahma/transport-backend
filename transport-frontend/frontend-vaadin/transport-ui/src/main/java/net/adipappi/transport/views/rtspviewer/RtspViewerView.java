package net.adipappi.transport.views.rtspviewer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.adipappi.transport.views.MainLayout;

@PageTitle("RTSP Viewer")
@Route(value = "rtsp-viewer", layout = MainLayout.class)
public class RtspViewerView extends VerticalLayout {
    
    private final IFrame videoFrame;
    private final TextField rtspUrlField;
    private final TextField streamNameField;
    
    public RtspViewerView() {
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        rtspUrlField = new TextField("URL du flux RTSP");
        rtspUrlField.setWidth("80%");
        rtspUrlField.setValue("rtsp://example.com/stream");
        
        streamNameField = new TextField("Nom du stream");
        streamNameField.setWidth("80%");
        streamNameField.setValue("stream1");
        
        videoFrame = new IFrame();
        videoFrame.setWidth("80%");
        videoFrame.setHeight("600px");
        videoFrame.getStyle().set("border", "none");
        
        Button startStreamButton = new Button("Démarrer le stream", e -> startStream());
        Button recognizeButton = new Button("Reconnaître visage", e -> recognizeFace());
        Button detectVehicleButton = new Button("Détecter véhicule", e -> detectVehicle());

        add(rtspUrlField, streamNameField, startStreamButton, 
            new HorizontalLayout(recognizeButton, detectVehicleButton), videoFrame);
    }
    
    private void startStream() {
        String rtspUrl = rtspUrlField.getValue();
        String streamName = streamNameField.getValue();
        Notification.show("Connexion au backend: http://localhost:8086/api/video/stream?rtspUrl=" + rtspUrl + "&streamName=" + streamName);
    }
    
    private void recognizeFace() {
        Notification.show("Fonctionnalité de reconnaissance à implémenter");
    }
    
    private void detectVehicle() {
        Notification.show("Détection de véhicule à implémenter");
    }
}
