import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Game extends Canvas implements Runnable {
	private final static long serialVersionUID = 1L;
	private final static int WIDTH = 256;
	private final static int HEIGHT = 256;
	private final static int SCALE = 2;
	
	private Random random = new Random();
	private boolean running = false;
	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	private int pixels[] = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	private int[] sheet;
	private int type[];
	private Input input = new Input(this);
	private boolean[] isTree;
	
	public void start() {
		running = true;
		new Thread(this).start();
	}
	
	private void init() {
		type = new int[16 * 16];
		isTree = new boolean[16 * 16];
		for (int i = 0; i < WIDTH / 16; ++i) {
			for (int j = 0; j < HEIGHT / 16; ++j) {
				type[i * 16 + j] = 3;
				/*if (random.nextInt(5) == 0) {
					type[i * 16 + j] = 4;
					isTree[i * 16 + j] = true;
				}*/
			}
		}
		try {
			BufferedImage img = ImageIO.read(Game.class.getResourceAsStream("/icons.png"));
			int w = img.getWidth();
			int h = img.getHeight();
			sheet = img.getRGB(0, 0, w, h, null, 0, w);
		} catch(IOException e) {
			e.printStackTrace();
		}
		//System.out.println(sheet[17]);
	}
	
	public void run()  {
		init();
		long timer = System.nanoTime();
		long mtimer = System.currentTimeMillis();
		//double nsPerTick = 1000000000.0 / 0.5;
		//double unprocessed = 0;
		//int ticks = 0;
		int frames = 0;
		
		while(running) {
			long now = System.nanoTime();
			//unprocessed += (now - timer) / nsPerTick;
			timer = now;
			/*while(unprocessed > 0) {
				tick();
				++ticks;
				--unprocessed;
			}*/
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			render();
			++frames;
			if (System.currentTimeMillis() - mtimer > 1000) {
				mtimer += 1000;
				//System.out.println(ticks + " ticks, " + frames + " fps");
				//frames = ticks = 0;
				frames = 0;
			}
		}
	}
	
	public void tick() {
	}
	
	public void renderTile(int xp, int yp, int tile) {
		int xtile = tile / 4;
		int ytile = tile % 4;
		int offset = xtile * 64 * 16 + ytile * 16;
		for (int i = 0; i < 16; ++i) {
			int x = xp + i;
			for (int j = 0; j < 16; ++j) {
				int y = yp + j;
				int color = sheet[i * 64 + j + offset];
				if (color != 0) {
					pixels[x * 256 + y] = color;	
				}
			}
		}
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			requestFocus();
			return;
		}
		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				renderTile(16 * i, 16 * j, type[i * 16 + j]);
				//renderTile(16 * i, 16 * j, 3);
				if (isTree[16 * i + j]) {
					renderTile(16 * i, 16 * j, 4);
				}
			}
		}
		Graphics g = bs.getDrawGraphics();
		g.fillRect(0, 0, getWidth(), getHeight());
		int ww = WIDTH * SCALE;
		int hh = HEIGHT * SCALE;
		int xo = (getWidth() - ww) / 2;
		int yo = (getHeight() - hh) / 2;
		g.drawImage(image, xo, yo, ww, hh, null);
		g.dispose();
		bs.show();
	}
	
	public boolean addTree() {
		ArrayList<Integer> nothing = new ArrayList<Integer>();
		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				if (!isTree[16 * i + j]) {
					nothing.add(16 * i + j);
				}
			}
		}
		//System.out.println("Checking");
		if (nothing.isEmpty())
			return false;
		int x = nothing.get(random.nextInt(nothing.size()));
		type[x] = 0;
		isTree[x] = true;
		if (random.nextInt(2) == 0 && x % 16 != 0 && x - 1 >= 0) type[x - 1] = 0;
		if (random.nextInt(2) == 0 && x - 16 >= 0) type[x - 16] = 0;
		if (random.nextInt(2) == 0 && x % 16 != 15 && x + 1 < 256) type[x + 1] = 0;
		if (random.nextInt(2) == 0 && x + 16 < 256) type[x + 16] = 0;
		return true;
	}
	
	public static void main(String[] args) {
		Game game = new Game();
		game.setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		game.setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		
		JFrame frame = new JFrame("Plant some trees");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(game, BorderLayout.CENTER);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		game.start();
	}
	
	public class Input implements MouseListener {
		public Input(Game game) {
			game.addMouseListener(this);
		}
		
	    @Override
	    public void mouseClicked(MouseEvent arg0) {
	    	addTree();
	    }

	    @Override
	    public void mouseEntered(MouseEvent arg0) { }

	    @Override
	    public void mouseExited(MouseEvent arg0) { }

	    @Override
	    public void mousePressed(MouseEvent arg0) { }

	    @Override
	    public void mouseReleased(MouseEvent arg0) { }

	}
}