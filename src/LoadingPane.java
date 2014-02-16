import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

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

/**
 * A component to be used as the glass pane of a container.
 * 
 * Once made visible it draws a translucent overlay over the container and
 * prevents interaction with the container's components until it is made not visible.
 * 
 * @author Daniel Gibbs <daniel@danielgibbs.name>
 */
public class LoadingPane extends JComponent implements MouseListener, FocusListener {

	public LoadingPane() {
		// Absorb mouse events.
		addMouseListener(this);
		// Maintain focus to absorb key events.
		addFocusListener(this);
	}

	public void paintComponent(Graphics g) {
		// Paint a grey translucent background.
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
		g.fillRect(0, 0, getWidth(), getHeight());
		// Paint a loading message in the center of the loading pane.
		g.setFont(g.getFont().deriveFont(32.0f));
		FontMetrics metrics = g.getFontMetrics();
		String message = "Loading...";
		g.setColor(Color.BLACK);
		g.drawString(message, (getWidth() - metrics.stringWidth(message)) / 2, ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent());
	}
	
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// Make sure the loading pane gets the focus.
		if (visible)
	    	requestFocusInWindow();
	}

	public void focusLost(FocusEvent e) {
		// Make sure the loading pane keeps the focus.
		if (isVisible())
			requestFocusInWindow();
	}
	
	public void focusGained(FocusEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
}
