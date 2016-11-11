package se.lu.lucs.sound;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class SoundService extends HttpServlet {
    private final static Generator GENERATOR = new Generator();

    private final static AudioFormat FORMAT = new AudioFormat( 44100, 16, 1, true, false );

    @Override
    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException {
        final HttpSession session = req.getSession( true );
        final List<BoutParameters> par = (List<BoutParameters>) session.getAttribute( "boutParameters" );
        final String numberString = req.getRequestURL().substring( req.getRequestURL().lastIndexOf( "/" ) + 1 ).replace( ".wav", "" );
        final int number = Integer.parseInt( numberString );
        final BoutParameters p = par.get( number );

        final List<Double> ampl = Generator.generateBout( p );

        AudioSystem.write( Generator.convertAmplitude( ampl, FORMAT ), AudioFileFormat.Type.WAVE, resp.getOutputStream() );

    }
}
