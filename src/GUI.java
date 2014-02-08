/*
 * Implementation of the chaos game to generate flame fractals
 * Copyright (c) 2014, Roman Klapaukh.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI extends JComponent {

	private static final long serialVersionUID = -8900010760721989010L;
	
	// Number of variations.
	private static final int NUM_VARIATIONS = 49;
	
	// Seed and random number generator.
	private final long initialSeed;
	private final Random random;
	
	// Rendering thread, lock, and recalculate flag.
	private Thread renderingThread;
	private Object renderLock = new Object();
	private boolean pendingRender = false;
	private boolean renderRecalculate;
	
	// Image buffer.
	private int bufferWidth = 1024;
	private int bufferHeight = 768;
	private BufferedImage image;
	private Graphics2D graphics;
	
	// GUI components.
	private JButton redrawButton;

	private double[][] coeffs;
	private double[][] postCoeffs;
	private double[][] funcColors;
	private long numIter = 100000;
	private Variation[] variations;
	private double[] variationWeights;
	private int count;
	private boolean done;
	private int zoom = 1;
	private double gamma = 4;
	private int[][] data;
	private double[][][] colors;
	private int superSampleSize = 3;

	public GUI() {
		// Use the current time as a seed.
		this(System.currentTimeMillis());
	}
	
	public GUI(long seed) {
		// Initialize the random number generator with the given seed.
		initialSeed = seed;
		random = new Random(seed);
		System.out.println("[INIT] Initializing Chaos Games with seed " + initialSeed);
		
		// Initialize co-efficients, colors, and variations.
		coeffs = new double[random.nextInt(10) + 2][6];
		postCoeffs = new double[coeffs.length][6];
		funcColors = new double[coeffs.length][3];
		for (int i = 0; i < coeffs.length; i++) {
			for (int j = 0; j < 6; j++) {
				coeffs[i][j] = random.nextGaussian();
				postCoeffs[i][j] = random.nextGaussian();
				if (j < funcColors[i].length) {
					funcColors[i][j] = random.nextDouble();
				}
			}
		}
		initializeVariations();

		// Create image buffer to hold render result before drawing to screen.
		image = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();
		
		// Create GUI.
		JFrame frame = new JFrame("Chaos Games - " + initialSeed);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this, BorderLayout.CENTER);

		// Create settings panel at the bottom.
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// Add slider to control the number of iterations.
		JPanel panel = new JPanel();
		JSlider slider = new JSlider(4, 10, 5);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				numIter = (long) Math.pow(10, ((JSlider) e.getSource()).getValue());
				requestRender(true);
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Iterations"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Add slider to control the zoom level.
		panel = new JPanel();
		slider = new JSlider(1, 10, 1);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				zoom = ((JSlider) e.getSource()).getValue();
				requestRender(true);
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Zoom"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Add slider to control the Gamma.
		panel = new JPanel();
		slider = new JSlider(1, 100, (int) (gamma * 10));
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gamma = ((JSlider) e.getSource()).getValue() / 10.0;
				requestRender(false);
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Gamma"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Add redraw button.
		redrawButton = new JButton("Redraw");
		redrawButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestRender(true);
			}
		});
		mainPanel.add(redrawButton);

		// Add top panel to set the weights for each variation.
		JPanel topPanel = new JPanel();
		for (int i = 0; i < variations.length; i++) {
			JLabel lab = new JLabel(variations[i].toString());
			final JTextField t = new JTextField(4);
			final int l = i;

			t.addKeyListener(new KeyAdapter() {
				int slot = l;
				JTextField f = t;

				@Override
				public void keyReleased(KeyEvent e) {
					try {
						variationWeights[slot] = Double.parseDouble(f.getText());
					} catch (Exception exp) {
						exp.printStackTrace();
					}
				}
			});

			t.setText(String.format("%.3f", variationWeights[i]));
			topPanel.add(lab);
			topPanel.add(t);
		}

		// Put the top panel is an scroll pane.
		JScrollPane scroll = new JScrollPane(topPanel);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroll.setPreferredSize(new Dimension(1024, 50));

		// Add the top panel and main panel to the window and display it.
		frame.getContentPane().add(scroll, BorderLayout.NORTH);
		frame.getContentPane().add(mainPanel, BorderLayout.SOUTH);
		frame.setMinimumSize(frame.getMinimumSize()); // http://stackoverflow.com/a/19507872/343486
		frame.pack();
		frame.setVisible(true);

		// Start rendering thread.
		renderingThread = new Thread() {
			public void run() {
				while (true) {
					synchronized (renderLock) {
						try {
							// Wait until a render is requested.
							renderLock.wait();
							// Clear the pending render flag and perform the render.
							pendingRender = false;
							System.out.println("[RENDER] Beginning render...");
							render(renderRecalculate);
							System.out.println("[RENDER] Done.");
							// Paint the rendered image to the screen.
							repaint();
						} catch (InterruptedException e) {
							System.out.println("\n[RENDER] Interrupted.");
							// The current render has been interrupted because there is a new render pending.
						}
					}
				}
			}
			
			/**
			 * Checks if there is a a new render pending.
			 * If there, is throw an exception so the current render stops.
			 * 
			 * @throws InterruptedException If there is a new render pending.
			 */
			private void checkForPending() throws InterruptedException {
				if (pendingRender)
					throw new InterruptedException();
			}
			
			/**
			 * Calculates and renders the flame fractal.
			 * 
			 * This method should periodically call checkForPending() to check
			 * if there is a new render pending, and if so, abort the current
			 * render and begin rendering again.
			 * 
			 * @param recalculate If the result of the variations should be recalculated.
			 * @throws InterruptedException If there is a new render pending.
			 */
			private void render(boolean recalculate) throws InterruptedException {
				System.out.print("[RENDER] Preparing for render...");
				done = false;
				count = 0;

				// Set the image buffer to all black.
				graphics.setColor(Color.BLACK);
				graphics.fillRect(0, 0, bufferWidth, bufferHeight);

				int h = bufferHeight * superSampleSize;
				int w = bufferWidth * superSampleSize;
				if (recalculate || data.length != w || data[0].length != h) {
					if (data == null || data.length != w || data[0].length != h) {
						data = new int[w][h];
						colors = new double[w][h][4];
					} else {
						for (int i = 0; i < w; i++) {
							for (int j = 0; j < h; j++) {
								checkForPending();
								data[i][j] = 0;
								for (int k = 0; k < 4; k++) {
									colors[i][j][k] = 0;
								}
							}
						}
					}

					double[] p = new double[] {
						random.nextDouble(), random.nextDouble()
					};
					double[] newp = new double[2];
					double[] totalp = new double[2];
					double[] col = new double[] {
						random.nextDouble(), random.nextDouble(), random.nextDouble()
					};
					for (int i = 0; i < 20 && !recalculate; i++) {
						checkForPending();
						int f = random.nextInt(coeffs.length - 1);
						applyAll(f, p, newp, totalp);
						col[0] = (col[0] + funcColors[f][0]) / 2.0;
						col[1] = (col[1] + funcColors[f][1]) / 2.0;
						col[2] = (col[2] + funcColors[f][2]) / 2.0;

						f = coeffs.length - 1;
						applyAll(f, p, newp, totalp);
						col[0] = (col[0] + funcColors[f][0]) / 2.0;
						col[1] = (col[1] + funcColors[f][1]) / 2.0;
						col[2] = (col[2] + funcColors[f][2]) / 2.0;
					}

					for (int i = 0; i < numIter && recalculate; i++) {
						checkForPending();
						System.out.print("\r[RENDER] " + (i+1) + "/" + numIter + " iterations completed.");
						int f = random.nextInt(coeffs.length - 1);
						applyAll(f, p, newp, totalp);
						col[0] = (col[0] + funcColors[f][0]) / 2.0;
						col[1] = (col[1] + funcColors[f][1]) / 2.0;
						col[2] = (col[2] + funcColors[f][2]) / 2.0;

						f = coeffs.length - 1;
						applyAll(f, p, newp, totalp);
						col[0] = (col[0] + funcColors[f][0]) / 2.0;
						col[1] = (col[1] + funcColors[f][1]) / 2.0;
						col[2] = (col[2] + funcColors[f][2]) / 2.0;

						if (!(p[0] > zoom || p[0] < -zoom || p[1] > zoom || p[1] < -zoom)) {
							int x = (int) (p[0] * w / (zoom * 2) + w / 2);
							int y = (int) (p[1] * h / (zoom * 2) + h / 2);
							data[x][y]++;
							colors[x][y][0] += col[0];
							colors[x][y][1] += col[1];
							colors[x][y][2] += col[2];
							colors[x][y][3]++;
						}
					}
					recalculate = false;
				}

				int ss = superSampleSize / 2, c = 0;
				boolean draw;
				for (int i = superSampleSize / 2; i < w; i += superSampleSize) {
					for (int j = superSampleSize / 2; j < h; j += superSampleSize) {
						float rt = 0, gt = 0, bt = 0;
						draw = false;
						c = 0;
						for (int x = i - ss; x <= i + ss; x++) {
							for (int y = j - ss; y <= j + ss; y++) {
								checkForPending();
								if (data[x][y] != 0) {
									draw = true;
									double alpha = Math.log(colors[x][y][3]) / colors[x][y][3];
									float r = (float) (alpha * colors[x][y][0]);
									float gr = (float) (alpha * colors[x][y][1]);
									float b = (float) (alpha * colors[x][y][2]);
									r = (float) Math.pow(r, gamma);
									gr = (float) Math.pow(gr, gamma);
									b = (float) Math.pow(b, gamma);
									r = Math.min(r, 1);
									gr = Math.min(gr, 1);
									b = Math.min(b, 1);
									rt += r;
									gt += gr;
									bt += b;
									c++;
								}
							}
						}
						rt /= c;
						gt /= c;
						bt /= c;
						if (draw) {
							count++;
							image.setRGB(i / superSampleSize, j / superSampleSize, new Color(rt, gt, bt).getRGB());
						}
					}
				}
				System.out.println("\n[RENDER] Painted " + count + " pixels at zoom level " + zoom);
				done = true;
			}
		};
		renderingThread.start();

		while (true) {
			while (!done) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			if (count < 10) {
				seed = random.nextLong();
				random.setSeed(seed);
				System.out.println("Seed: " + seed);
				coeffs = new double[random.nextInt(10) + 2][6];
				postCoeffs = new double[coeffs.length][6];
				funcColors = new double[coeffs.length][3];
				for (int i = 0; i < coeffs.length; i++) {
					for (int j = 0; j < 6; j++) {
						coeffs[i][j] = random.nextGaussian();
						postCoeffs[i][j] = random.nextGaussian();
						if (j < funcColors[i].length) {
							funcColors[i][j] = random.nextDouble();
						}
					}
				}
				initializeVariations();
				done = false;
				requestRender(true);
			} else {
				return;
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(bufferWidth, bufferHeight);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(320, 240);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, bufferWidth, bufferHeight, null);
	}
	
	/**
	 * Requests that the flame fractal is re-rendered.
	 * This may interrupt an existing render.
	 * 
	 * @param recalculate If the result of the variations should be recalculated.
	 */
	private void requestRender(boolean recalculate) {
		// Set the pending render flag.
		pendingRender = true;
		synchronized (renderLock) {
			// Set the recalculate flag.
			renderRecalculate = recalculate;
			// Notify the rendering thread that a render is pending.
			renderLock.notify();
		}
	}

	public void applyAll(int f, double[] p, double[] newp, double[] totalp) {
		newp[0] = coeffs[f][0] * p[0] + coeffs[f][1] * p[1] + coeffs[f][2];
		newp[1] = coeffs[f][3] * p[0] + coeffs[f][4] * p[1] + coeffs[f][5];
		p[0] = newp[0];
		p[1] = newp[1];

		// Apply ALL the variations with their respective weights.
		totalp[0] = 0;
		totalp[1] = 0;
		double r = Math.sqrt(p[0] * p[0] + p[1] * p[1]);
		double theta = Math.atan(p[0] / p[1]);
		double phi = Math.atan(p[1] / p[0]);

		boolean var = false;
		for (int v = 0; v < variations.length; v++) {
			if (Double.compare(variationWeights[v], 0) != 0) {
				var = true;
				variations[v].apply(p, newp, coeffs[f], r, theta, phi, random);
				totalp[0] += newp[0] * variationWeights[v];
				totalp[1] += newp[1] * variationWeights[v];
			}
		}
		if (var) {
			p[0] = totalp[0];
			p[1] = totalp[1];
		}

		// Post transform.
		newp[0] = postCoeffs[f][0] * p[0] + postCoeffs[f][1] * p[1] + postCoeffs[f][2];
		newp[1] = postCoeffs[f][3] * p[0] + postCoeffs[f][4] * p[1] + postCoeffs[f][5];
		p[0] = newp[0];
		p[1] = newp[1];
	}

	/**
	 * Initialize all the different variations with random parameters,
	 * and generate normalized random weights for each variation.
	 */
	public void initializeVariations() {
		// Normalize all the weights.
		variationWeights = new double[NUM_VARIATIONS];
		double total = 0;
		for (int i = 0; i < NUM_VARIATIONS; i++) {
			if (random.nextDouble() < 0.3) {
				variationWeights[i] = random.nextDouble() + 0.1;
				total += variationWeights[i];
			} else {
				variationWeights[i] = 0;
			}
		}
		for (int i = 0; i < NUM_VARIATIONS; i++) {
			variationWeights[i] /= total;
		}

		// Initialize all the variations.
		variations = new Variation[NUM_VARIATIONS];
		variations[0] = new Variation.Linear();
		variations[1] = new Variation.Sinusoidal();
		variations[2] = new Variation.Spherical();
		variations[3] = new Variation.Swirl();
		variations[4] = new Variation.Horseshoe();
		variations[5] = new Variation.Polar();
		variations[6] = new Variation.Handkerchief();
		variations[7] = new Variation.Heart();
		variations[8] = new Variation.Disc();
		variations[9] = new Variation.Spiral();
		variations[10] = new Variation.Hyperbolic();
		variations[11] = new Variation.Diamond();
		variations[12] = new Variation.Ex();
		variations[13] = new Variation.Julia();
		variations[14] = new Variation.Bent();
		variations[15] = new Variation.Waves();
		variations[16] = new Variation.Fisheye();
		variations[17] = new Variation.Popcorn();
		variations[18] = new Variation.Exponential();
		variations[19] = new Variation.Power();
		variations[20] = new Variation.Cosine();
		variations[21] = new Variation.Rings();
		variations[22] = new Variation.Fan();
		variations[23] = new Variation.Blob(random.nextDouble(), random.nextDouble(), random.nextDouble());
		variations[24] = new Variation.PDJ(random.nextDouble(), random.nextDouble(), random.nextDouble(), random.nextDouble());
		variations[25] = new Variation.Fan2(random.nextDouble(), random.nextDouble());
		variations[26] = new Variation.Rings2(random.nextDouble());
		variations[27] = new Variation.Eyefish();
		variations[28] = new Variation.Bubble();
		variations[29] = new Variation.Cylinder();
		variations[30] = new Variation.Perspective(random.nextDouble() * Math.PI * 2, random.nextGaussian());
		variations[31] = new Variation.Noise();
		variations[32] = new Variation.JuliaN(random.nextGaussian(), random.nextGaussian());
		variations[33] = new Variation.JuliaScope(random.nextGaussian(), random.nextGaussian());
		variations[34] = new Variation.Blur();
		variations[35] = new Variation.Gaussian();
		variations[36] = new Variation.RadialBlur(random.nextDouble() * Math.PI * 2, variationWeights[36]);
		variations[37] = new Variation.Pie(random.nextInt(10), random.nextDouble() * Math.PI * 2, random.nextDouble());
		variations[38] = new Variation.Ngon(random.nextDouble() * 5, random.nextInt(10), random.nextInt(12), random.nextGaussian());
		variations[39] = new Variation.Curl(random.nextGaussian(), random.nextGaussian());
		variations[40] = new Variation.Rectangles(random.nextGaussian(), random.nextGaussian());
		variations[41] = new Variation.Arch(variationWeights[41]);
		variations[42] = new Variation.Tangent();
		variations[43] = new Variation.Square();
		variations[44] = new Variation.Rays(variationWeights[44]);
		variations[45] = new Variation.Blade(variationWeights[45]);
		variations[46] = new Variation.Secant(variationWeights[46]);
		variations[47] = new Variation.Twintrian(variationWeights[47]);
		variations[48] = new Variation.Cross();
	}
	
	public static void main(String args[]) {
		new GUI();
	}
}
