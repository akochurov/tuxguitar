package org.herac.tuxguitar.io.gervill;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.herac.tuxguitar.song.models.TGDuration;

public class MidiToAudioWriter {
	
	public static void write(OutputStream out, List<MidiEvent> events, MidiToAudioSettings settings) throws Throwable {
		MidiToAudioSynth.instance().openSynth();
		
		int usqTempo = 60000000 / 120;
		long previousTick = 0;
		long timePosition = 0;
		MidiToAudioWriter.sort(events);
		Receiver receiver = MidiToAudioSynth.instance().getReceiver();
		AudioInputStream stream = MidiToAudioSynth.instance().getStream();

        for (MidiEvent event : events) {
            MidiMessage msg = event.getMessage();

            timePosition += ((event.getTick() - previousTick) * usqTempo) / TGDuration.QUARTER_TIME;

            if (msg instanceof MetaMessage) {
                if (((MetaMessage) msg).getType() == 0x51) {
                    byte[] data = ((MetaMessage) msg).getData();
                    usqTempo = ((data[0] & 0xff) << 16) | ((data[1] & 0xff) << 8) | (data[2] & 0xff);
                }
            } else {
                receiver.send(msg, timePosition);
            }
            previousTick = event.getTick();
        }
		
		long duration = (long) (stream.getFormat().getFrameRate() * ( (timePosition / 1000000.0) ));
		
		AudioInputStream srcStream = new AudioInputStream(stream, stream.getFormat(), duration );
		AudioInputStream dstStream = AudioSystem.getAudioInputStream(settings.getFormat(), srcStream );
		AudioSystem.write(new AudioInputStream(dstStream, dstStream.getFormat(), duration ), settings.getType(), out);
		
		dstStream.close();
		srcStream.close();
		
		MidiToAudioSynth.instance().closeSynth();
	}
	
	private static void sort(List<MidiEvent> events){
		Collections.sort(events, new Comparator<MidiEvent>() {
			public int compare(MidiEvent e1, MidiEvent e2) {
				if( e1 != null && e2 != null){
					if(e1.getTick() > e2.getTick()){
						return 1;
					}
					else if(e1.getTick() < e2.getTick()){
						return -1;
					}
				}
				return 0;
			}
		});
	}
}
