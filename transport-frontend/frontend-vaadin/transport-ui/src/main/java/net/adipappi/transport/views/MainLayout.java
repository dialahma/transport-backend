package net.adipappi.transport.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import net.adipappi.transport.views.rtspviewer.RtspViewerView;

public class MainLayout extends AppLayout {
    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Transport UI");
        logo.addClassNames("text-l", "m-m");

        HorizontalLayout header = new HorizontalLayout(
            new DrawerToggle(), 
            logo
        );

        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        addToDrawer(new VerticalLayout(
            new RouterLink("RTSP Viewer", RtspViewerView.class)
        ));
    }


    //@PageBody
    private void initPlayerJs() {
        UI.getCurrent().getPage().executeJs("""
        window.HlsJsPlayer = class {
            constructor(config) {
                this.parent = config.parent;
                this.url = config.url;
                this.createPlayer();
            }
            
            createPlayer() {
                const video = document.createElement('video');
                video.controls = true;
                video.autoplay = true;
                video.muted = true;
                
                if (video.canPlayType('application/vnd.apple.mpegurl')) {
                    // Safari natif
                    video.src = this.url;
                } else if (Hls.isSupported()) {
                    // HLS.js pour les autres navigateurs
                    const hls = new Hls();
                    hls.loadSource(this.url);
                    hls.attachMedia(video);
                }
                
                this.parent.appendChild(video);
            }
        }
        
        // Charger HLS.js si nécessaire
        if (!window.Hls && !video.canPlayType('application/vnd.apple.mpegurl')) {
            const script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest';
            document.head.appendChild(script);
        }
        """);
    }
}
