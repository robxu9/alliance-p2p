package org.alliance.ui;

import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;

public class UIOpeningSound{
	private File soundFile;
   public UIOpeningSound(File midifile)
    {
	   this.soundFile = midifile;
    }
   	public void playOpeningSound()
   	{
    	try {
            // From file
            Sequence sequence = MidiSystem.getSequence(soundFile);
            // Create a sequencer for the sequence
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(sequence);
        
            // Start playing
            sequencer.start();
        } catch (IOException e) {
        } catch (MidiUnavailableException e) {
        } catch (InvalidMidiDataException e) {
        }
    }
}