import java.util.*;
import javax.sound.sampled.*;
import javazoom.jl.player.advanced.*;

/**
 * Creates a thread which plays music and sound effects.
 * 
 * @author Stephen
 */
public class MusicThread extends Thread {
	/**
	 * Map of loaded sounds.
	 */
	private Map<String, Object> map;
	/**
	 * List of songs to play.
	 */
	private List<String> toPlay;
	/**
	 * Place to get songs.
	 */
	private ResourceGetter res;
	/**
	 * A child thread for MP3 playing.
	 */
	private MusicThread child;
	/**
	 * Whether the thread is running.
	 */
	private volatile boolean running;
	/**
	 * The method to play MP3s with JLayer.
	 */
	private AdvancedPlayer mp3player;
	/**
	 * Whether loop is on.
	 */
	private boolean loop;

	/**
	 * Creates a music thread that will get songs from the given place.
	 * 
	 * @param res the resource getter to find files
	 */
	public MusicThread(ResourceGetter res) {
		super("Music Thread");
		running = false;
		setPriority(Thread.MIN_PRIORITY + 1);
		setDaemon(true);
		map = new HashMap<String, Object>(64);
		this.res = res;
		toPlay = new LinkedList<String>();
		child = new MusicThread(this);
		mp3player = null;
		child.start();
		loop = false;
	}
	/**
	 * Used for making child music threads for MP3s.
	 * 
	 * @param parent the parent thread
	 */
	private MusicThread(MusicThread parent) {
		super("MP3 Thread");
		running = false;
		setPriority(Thread.MIN_PRIORITY);
		setDaemon(true);
		map = new HashMap<String, Object>(64);
		this.res = parent.res;
		toPlay = new LinkedList<String>();
		child = null;
		mp3player = null;
		loop = false;
	}
	/**
	 * Gets whether this music thread is currently blocked playing a song.
	 * 
	 * @return whether the child thread is playing a MP3
	 */
	private boolean isRunning() {
		return running;
	}
	/**
	 * Checks to see if an MP3 is playing.
	 * 
	 * @return whether an MP3 is playing
	 */
	public boolean mp3Playing() {
		return child.isRunning();
	}
	/**
	 * Sets whether the sound track will loop.
	 * 
	 * @param loop whether the sound track will globally loop
	 */
	public void setLoop(boolean loop) {
		if (child != null) child.setLoop(loop);
		else this.loop = loop;
	}
	/**
	 * Stops all sounds!
	 */
	public void stopMusic() {
		toPlay.clear();
		if (child != null) child.stopMusic();
		else if (mp3player != null && running) mp3player.close();
	}
	public void run() {
		String song;
		if (child == null) while (true) {
			while (toPlay.size() < 1) Utils.sleep(5L);
			synchronized (toPlay) {
				song = toPlay.remove(0);
				if (loop) toPlay.add(song);
			}
			mp3player = (AdvancedPlayer)map.get(song);
			if (mp3player != null) try {
				running = true;
				load(song);
				mp3player.play(0, Integer.MAX_VALUE);
				running = false;
				mp3player = null;
			} catch (Exception e) { }
		} else while (true) {
			synchronized (toPlay) {
				Iterator<String> it = toPlay.iterator();
				while (it.hasNext()) {
					song = it.next();
					if (song.endsWith(".mp3") && child != null) {
						// pass mp3 to child
						child.queueMusic(song);
					} else {
						Clip clip = (Clip)map.get(song);
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
	 * Loads the given sound effect.
	 * 
	 * @param file the file to load
	 */
	public synchronized void load(String file) {
		if (file.endsWith(".mp3")) {
			if (child == null) try {
				map.put(file, new AdvancedPlayer(res.getResource("sound/" + file)));
			} catch (Exception e) {
				map.put(file, null);
			} else
				child.load(file);
		} else try {
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(res.getResource("sound/" + file)));
			clip.setFramePosition(0);
			clip.setLoopPoints(0, -1);
			map.put(file, clip);
		} catch (Exception e) {
			map.put(file, null);
		}
	}
	/**
	 * Queues the given music item. Music <b>must</b> first be loaded, <i>no exceptions.</i>
	 * 
	 * @param song the 
	 */
	public void queueMusic(String song) {
		synchronized (toPlay) {
			toPlay.add(song);
		}
	}
	/**
	 * Starts the given sound effect.
	 *  <b>There will be a delay if it was not loaded prior to starting!!!</b>
	 * 
	 * @param name the sound file name to play
	 */
	public void startSFX(String name) {
		if (!map.containsKey(name))
			load(name);
		synchronized (toPlay) {
			toPlay.add(name);
		}
	}
}