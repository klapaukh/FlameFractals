import java.awt.Color;

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
 * A flame function, comprising of 6 coefficients, 6 post coefficients,
 * and a function color.
 */
public class FlameFunction {
	public final double[] coefficients;
	public final double[] postCoefficients;
	public final Color color;

	public FlameFunction(double[] coefficients, double[] postCoefficients, Color color) {
		this.coefficients = coefficients;
		this.postCoefficients = postCoefficients;
		this.color = color;
	}
}
