import java.util.*;
import javax.sound.sampled.*;

/**
 * Creates a thread which plays music and sound effects.
 * 
 * @author Stephen
 */
public class MusicThread extends Thread {
	/**
	 * Map of loaded sounds.
	 */
	private Map<String, Clip> map;
	/**
	 * List of songs to play.
	 */
	private List<String> toPlay;
	/**
	 * Place to get songs.
	 */
	private ResourceGetter res;

	/**
	 * Creates a music thread that will get songs from the given place.
	 * 
	 * @param res the resource getter to find files
	 */
	public MusicThread(ResourceGetter res) {
		super("Music Thread");
		setPriority(Thread.MIN_PRIORITY + 1);
		setDaemon(true);
		map = new HashMap<String, Clip>(64);
		this.res = res;
		toPlay = new LinkedList<String>();
	}
	public void run() {
		while (true) {
			synchronized (this) {
				Iterator<String> it = toPlay.iterator();
				String song;
				while (it.hasNext()) {
					song = it.next();
					if (song.endsWith(".mp3")); // no mp3s yet
					else {
						Clip clip = map.get(song);
						if (clip != null) {
							// wav
							clip.stop();
							clip.setFramePosition(0);
							clip.start();
						}
					}
					it.remove();
				}
			}
			Utils.sleep(5L);
		}
	}
	/**
	 * Loads the given song.
	 * 
	 * @param file the file to load
	 */
	public synchronized void load(String file) {
		try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(res.getResource(file)));
			clip.setFramePosition(0);
			clip.setLoopPoints(0, -1);
			map.put(file, clip);
		} catch (Exception e) {
			map.put(file, null);
		}
	}
	/**
	 * Queues the given music item.
	 * 
	 * @param song the 
	 */
	public synchronized void queueMusic(String song) {
		toPlay.add(song);
	}
	/**
	 * Starts the given sound effect.
	 *  <b>There will be a delay if it was not loaded prior to starting!!!</b>
	 * 
	 * @param name the sound file name to play
	 */
	public synchronized void startSFX(String name) {
		if (!map.containsKey(name))
			load(name);
		toPlay.add(name);
	}
}