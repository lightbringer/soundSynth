package se.lu.lucs.sound;

import java.applet.Applet;
import java.awt.BorderLayout;

public class TonSchmiedeApplet extends Applet {

    @Override
    public void init() {
        super.init();

        setLayout( new BorderLayout() );
        add( new TonSchmiede(), BorderLayout.CENTER );
    }

}
