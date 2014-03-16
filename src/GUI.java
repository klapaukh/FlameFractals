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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI extends JComponent {

	private static final long serialVersionUID = -8900010760721989010L;

	// Parameter bounds.
	private static final long MIN_ITERATIONS = 10000L; // 10^4
	private static final long MAX_ITERATIONS = 10000000000L; // 10^10
	private static final int MIN_ZOOM = 1;
	private static final int MAX_ZOOM = 10;
	private static final double MIN_GAMMA = 0.1;
	private static final double MAX_GAMMA = 5.0;

	// Variations and weights.
	private static final int NUM_VARIATIONS = 49;
	private Variation[] variations;
	private double[] variationWeights;

	// Seed and random number generator.
	private long seed;
	private Random random;

	// Current rendering task and executor.
	private FutureTask<Long> renderTask;
	private ExecutorService renderer;

	// Image buffer.
	private int bufferWidth = 1024;
	private int bufferHeight = 768;
	private BufferedImage image;
	private Graphics2D graphics;

	// GUI components.
	private JFrame frame;
	private LoadingPane loadingPane;
	private JButton redrawButton;
	private JProgressBar progressBar;

	// Flame functions.
	private int numFunctions = 6;
	private FlameFunction[] functions;

	private long numIterations = 100000L;
	private int zoom = 1;
	private double gamma = 2.2;
	private int[][] data;
	private double[][][] colors;
	private int superSampleSize = 3;

	public GUI() {
		// Use the current time as a seed.
		this(System.currentTimeMillis());
	}

	public GUI(long seed) {
		// Initialize variations and functions.
		initialize(seed);

		// Create image buffer to hold render result before drawing to screen.
		image = new BufferedImage(bufferWidth, bufferHeight, BufferedImage.TYPE_INT_RGB);
		graphics = image.createGraphics();

		// Create new executor to perform rendering in the background.
		renderer = Executors.newFixedThreadPool(1);

		// Create GUI.
		frame = new JFrame("Chaos Games - " + seed);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(this, BorderLayout.CENTER);

		// Create glass pane to overlay the frame during loading.
		loadingPane = new LoadingPane();
		frame.setGlassPane(loadingPane);

		// Create settings panel at the bottom.
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// Add progress bar.
		progressBar = new JProgressBar(0, 1000);
		progressBar.setStringPainted(true);
		mainPanel.add(progressBar);

		// Add slider to control the number of iterations.
		JPanel panel = new JPanel();
		JSlider slider = new JSlider((int)Math.log10(MIN_ITERATIONS), (int)Math.log10(MAX_ITERATIONS), (int)Math.log10(numIterations));
		slider.setMajorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				long newNumIter = (long) Math.pow(10, ((JSlider) e.getSource()).getValue());
				if(newNumIter != numIterations) {
					numIterations = newNumIter;
					render(true, false);
				}
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Iterations"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Add slider to control the zoom level.
		panel = new JPanel();
		slider = new JSlider(MIN_ZOOM, MAX_ZOOM, zoom);
		slider.setMajorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int newZoom = ((JSlider) e.getSource()).getValue();
				if(newZoom != zoom) {
					zoom = newZoom;
					render(true, false);
				}
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Zoom"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Add slider to control the Gamma.
		panel = new JPanel();
		slider = new JSlider((int)(MIN_GAMMA * 10), (int)(MAX_GAMMA * 10), (int)(gamma * 10));
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gamma = ((JSlider) e.getSource()).getValue() / 10.0;
				render(false, false);
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
				render(true, false);
			}
		});
		mainPanel.add(redrawButton);

		// Add redraw button.
		JButton reinitializeButton = new JButton("Reinitialize");
		reinitializeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initialize(random.nextLong());
				render(true, false);
			}
		});
		mainPanel.add(reinitializeButton);

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
						initializeVariations();
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

		// Add the loading pane to disable use of the components until the initialization has finished.
		loadingPane.setVisible(true);

		// Re-initialize variations and functions until there is a result with more than 10 pixels.
		while(render(true, true) < 10) {
			initialize(random.nextLong());
		}

		// Remove the loading pane.
		loadingPane.setVisible(false);
	}

	/**
	 * A task that calculates and renders the flame fractal with the current parameters.
	 * The task may be interrupted if a new render is requested before the current one has finished.
	 */
	private class RenderTask implements Callable<Long> {
		private boolean recalculate;

		public RenderTask(boolean recalculate) {
			this.recalculate = recalculate;
		}

		private void checkForInterrupted() throws InterruptedException {
			if(Thread.currentThread().isInterrupted()) {
				throw new InterruptedException();
			}
		}

		@Override
		public Long call() throws InterruptedException {
			long count = 0;

			// Create a deterministic RNG for the render.
			Random rnd = new Random(seed + 1);

			// Set the image buffer to all black.
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, bufferWidth, bufferHeight);

			// Reset the progress bar.
			progressBar.setValue(0);
			progressBar.setString("Preparing calculations...");

			int h = bufferHeight * superSampleSize;
			int w = bufferWidth * superSampleSize;
			if (recalculate || data.length != w || data[0].length != h) {
				if (data == null || data.length != w || data[0].length != h) {
					data = new int[w][h];
					colors = new double[w][h][4];
				} else {
					for (int i = 0; i < w; i++) {
						for (int j = 0; j < h; j++) {
							data[i][j] = 0;
							for (int k = 0; k < 4; k++) {
								colors[i][j][k] = 0;
							}
						}
					}
				}

				checkForInterrupted();

				double[] p = new double[] {
						rnd.nextDouble(), rnd.nextDouble()
				};
				double[] newp = new double[2];
				double[] totalp = new double[2];
				double[] col = new double[] {
						rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()
				};
				for (int i = 0; i < 20 && !recalculate; i++) {
					checkForInterrupted();
					FlameFunction f = functions[rnd.nextInt(numFunctions)];
					applyAll(f, p, newp, totalp, rnd);
					col[0] = (col[0] + (f.color.getRed() / 255.0)) / 2.0;
					col[1] = (col[1] + (f.color.getGreen() / 255.0)) / 2.0;
					col[2] = (col[2] + (f.color.getBlue() / 255.0)) / 2.0;
				}
				long startTime = System.currentTimeMillis();
				int realZoom = (MAX_ZOOM + 1) - zoom;
				for (long i = 0; i < numIterations && recalculate; i++) {
					if(numIterations % (numIterations / 1000) == 0) {
						progressBar.setValue((int)((i * 1000) / numIterations));
						long soFar = System.currentTimeMillis() - startTime;
						double perIteration = soFar / (double)i;
						long remaining = (long)Math.ceil(perIteration * (numIterations - i) / 1000);
						progressBar.setString("Estimated time remaining: " + remaining + " seconds.");
					}
					checkForInterrupted();
					FlameFunction f = functions[rnd.nextInt(numFunctions)];
					applyAll(f, p, newp, totalp, rnd);
					col[0] = (col[0] + (f.color.getRed() / 255.0)) / 2.0;
					col[1] = (col[1] + (f.color.getGreen() / 255.0)) / 2.0;
					col[2] = (col[2] + (f.color.getBlue() / 255.0)) / 2.0;

					if (!(p[0] > realZoom || p[0] < -realZoom || p[1] > realZoom || p[1] < -realZoom)) {
						int x = (int) (p[0] * w / (realZoom * 2) + w / 2);
						int y = (int) (p[1] * h / (realZoom * 2) + h / 2);
						data[x][y]++;
						colors[x][y][0] += col[0];
						colors[x][y][1] += col[1];
						colors[x][y][2] += col[2];
						colors[x][y][3]++;
					}
				}
				recalculate = false;
			}

			progressBar.setString("Rendering...");

			int ss = superSampleSize / 2, c = 0;
			boolean draw;
			for (int i = superSampleSize / 2; i < w; i += superSampleSize) {
				for (int j = superSampleSize / 2; j < h; j += superSampleSize) {
					float rt = 0, gt = 0, bt = 0;
					draw = false;
					c = 0;
					for (int x = i - ss; x <= i + ss; x++) {
						for (int y = j - ss; y <= j + ss; y++) {
							checkForInterrupted();
							if (data[x][y] != 0) {
								draw = true;
								double alpha = Math.log(colors[x][y][3]) / colors[x][y][3];
								float r = (float) (alpha * colors[x][y][0]);
								float gr = (float) (alpha * colors[x][y][1]);
								float b = (float) (alpha * colors[x][y][2]);
								r = (float) Math.pow(r, 1.0/gamma);
								gr = (float) Math.pow(gr, 1.0/gamma);
								b = (float) Math.pow(b, 1.0/gamma);
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
			progressBar.setValue(1000);
			progressBar.setString("Rendered " + count + " pixels at zoom level " + zoom);
			repaint();
			return count;
		}
	}

	private synchronized long render(boolean recalculate, boolean block) {
		if(renderTask != null && !renderTask.isDone()) {
			renderTask.cancel(true);
		}
		renderTask = new FutureTask<>(new RenderTask(recalculate));
		renderer.execute(renderTask);
		if(!block)
			return -1;
		try {
			return renderTask.get();
		} catch(Exception e) {
			return -1;
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

	public void applyAll(FlameFunction f, double[] p, double[] newp, double[] totalp, Random rnd) {
		newp[0] = f.coefficients[0] * p[0] + f.coefficients[1] * p[1] + f.coefficients[2];
		newp[1] = f.coefficients[3] * p[0] + f.coefficients[4] * p[1] + f.coefficients[5];
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
				variations[v].apply(p, newp, f.coefficients, r, theta, phi, rnd);
				totalp[0] += newp[0] * variationWeights[v];
				totalp[1] += newp[1] * variationWeights[v];
			}
		}
		if (var) {
			p[0] = totalp[0];
			p[1] = totalp[1];
		}

		// Post transform.
		newp[0] = f.postCoefficients[0] * p[0] + f.postCoefficients[1] * p[1] + f.postCoefficients[2];
		newp[1] = f.postCoefficients[3] * p[0] + f.postCoefficients[4] * p[1] + f.postCoefficients[5];
		p[0] = newp[0];
		p[1] = newp[1];
	}

	/**
	 * Initialize all the different variations with random parameters,
	 * and generate normalized random weights for each variation.
	 * Then initialize a random number of flame functions with random coefficients.
	 *
	 * @param seed The seed with which to initialize the random number generator.
	 */
	private synchronized void initialize(long seed) {
		// Initialize the random number generator with the given seed.
		this.seed = seed;
		random = new Random(seed);
		System.out.println("[INIT] Initializing Chaos Games with seed " + seed);
		if (frame != null) {
			frame.setTitle("Chaos Games - " + seed);
		}

		initializeVariations();
		initializeFlameFunctions();
	}

	private synchronized void initializeVariations() {
//		// Create Sierpinski's gasket.
//		variationWeights = new double[1];
//		variationWeights[0] = 1.0;
//		variations = new Variation[1];
//		variations[0] = new Variation.Linear();
//		numFunctions = 3;
//		functions = new FlameFunction[3];
//		functions[0] = new FlameFunction(new double[] {0.5, 0, 0, 0, 0.5, 0}, new double[] {1, 0, 0, 0, 1, 0}, Color.RED);
//		functions[1] = new FlameFunction(new double[] {0.5, 0, 0.5, 0, 0.5, 0}, new double[] {1, 0, 0, 0, 1, 0}, Color.GREEN);
//		functions[2] = new FlameFunction(new double[] {0.5, 0, 0, 0, 0.5, 0.5}, new double[] {1, 0, 0, 0, 1, 0}, Color.BLUE);

		// Normalize all the weights.
		variationWeights = new double[NUM_VARIATIONS];
		double total = 0;
		for (int i = 0; i < NUM_VARIATIONS; i++) {
			if (random.nextDouble() < 0.3) {
				variationWeights[i] = random.nextDouble() + 0.1;
				total += variationWeights[i];
			} else {
				variationWeights[i] = 0.0;
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

	private void initializeFlameFunctions() {
		// Initialize the flame functions.
		functions = new FlameFunction[numFunctions];
		for(int i = 0; i < numFunctions; i++) {
			double[] coefficients = new double[6];
			double[] postCoefficients = new double[6];
			for(int j = 0; j < 6; j++) {
				coefficients[j] = random.nextGaussian();
				postCoefficients[j] = random.nextGaussian();
			}
			Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
			functions[i] = new FlameFunction(coefficients, postCoefficients, color);
		}
	}

	public static void main(String args[]) {
		new GUI();
	}
}
