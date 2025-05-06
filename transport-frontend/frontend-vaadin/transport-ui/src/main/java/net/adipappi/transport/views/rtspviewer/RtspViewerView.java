package net.adipappi.transport.views.rtspviewer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import net.adipappi.transport.services.BackendService;
import net.adipappi.transport.views.MainLayout;

@PageTitle("RTSP Viewer")
@Route(value = "rtsp-viewer", layout = MainLayout.class)
public class RtspViewerView extends VerticalLayout {

    private final IFrame videoFrame;
    private final TextField rtspUrlField;
    private final TextField streamNameField;
    private final BackendService backendService;
    private final Div videoContainer;


    public RtspViewerView(BackendService backendService) {
        this.backendService = backendService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);

        rtspUrlField = new TextField("URL du flux RTSP");
        rtspUrlField.setWidth("80%");
        rtspUrlField.setValue("rtsp://wesadmin:PiCaM*_*1187@adipappi.media:8564/picamv21");

        streamNameField = new TextField("Nom du stream");
        streamNameField.setWidth("80%");
        streamNameField.setValue("stream1");

        videoFrame = new IFrame();
        videoFrame.setWidth("80%");
        videoFrame.setHeight("600px");
        videoFrame.getStyle().set("border", "none");

        videoContainer = new Div();
        videoContainer.setWidth("80%");
        videoContainer.setHeight("600px");
        videoContainer.getStyle().set("border", "1px solid #ccc");

        Button startStreamButton = new Button("Démarrer le stream HLS", e -> startHlsStream());
        Button recognizeButton = new Button("Reconnaître visage", e -> recognizeFace());
        Button detectVehicleButton = new Button("Détecter véhicule", e -> detectVehicle());

        add(rtspUrlField, streamNameField, startStreamButton,
                new HorizontalLayout(recognizeButton, detectVehicleButton), videoContainer);
    }

    private void startHlsStream() {
        String rtspUrl = rtspUrlField.getValue();
        String streamName = streamNameField.getValue();

        try {
            String result = backendService.startHlsStreaming(rtspUrl, streamName);
            Notification.show(result);

            String hlsUrl = "http://localhost:8085/api/video/hls/" + streamName + ".m3u8";

            videoContainer.getElement().executeJs("""
                        const container = this;
                        const url = $0;
                    
                        function loadHlsPlayer() {
                            const video = document.createElement('video');
                            video.controls = true;
                            video.autoplay = true;
                            video.style.width = '100%';
                            video.style.height = '100%';
                    
                            if (video.canPlayType('application/vnd.apple.mpegurl')) {
                                video.src = url;
                                video.load();
                            } else if (window.Hls) {
                                const hls = new Hls();
                                hls.loadSource(url);
                                hls.attachMedia(video);
                            } else {
                                console.error("HLS.js non chargé.");
                                return;
                            }
                    
                            container.innerHTML = '';
                            container.appendChild(video);
                        }
                    
                        if (!window.Hls) {
                            const script = document.createElement('script');
                            script.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest';
                            script.onload = loadHlsPlayer;
                            document.head.appendChild(script);
                        } else {
                            loadHlsPlayer();
                        }
                    """, hlsUrl);

        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void recognizeFace() {
        Notification.show("Fonctionnalité de reconnaissance à implémenter");
    }

    private void detectVehicle() {
        Notification.show("Détection de véhicule à implémenter");
    }
}

