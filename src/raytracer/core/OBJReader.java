package raytracer.core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import raytracer.core.def.Accelerator;
import raytracer.core.def.StandardObj;
import raytracer.math.Vec3;
import raytracer.math.Point;
import raytracer.geom.Primitive;
import raytracer.geom.GeomFactory;

/**
 * Represents a model file reader for the OBJ format
 */
public class OBJReader {

	/**
	 * Reads an OBJ file and uses the given shader for all triangles. While
	 * loading the triangles they are inserted into the given acceleration
	 * structure accelerator.
	 *
	 * @param filename
	 *                    The file to read the data from
	 * @param accelerator
	 *                    The target acceleration structure
	 * @param shader
	 *                    The shader which is used by all triangles
	 * @param scale
	 *                    The scale factor which is responsible for scaling the
	 *                    model
	 * @param translate
	 *                    A vector representing the translation coordinate with
	 *                    which
	 *                    all coordinates have to be translated
	 * @throws IllegalArgumentException
	 *                                  If the filename is null or the empty string,
	 *                                  the accelerator
	 *                                  is null, the shader is null, the translate
	 *                                  vector is null,
	 *                                  the translate vector is not finite or scale
	 *                                  does not
	 *                                  represent a legal (finite) floating point
	 *                                  number
	 */
	public static void read(final String filename,
			final Accelerator accelerator, final Shader shader, final float scale,
			final Vec3 translate) throws FileNotFoundException {
		read(new BufferedInputStream(new FileInputStream(filename)), accelerator, shader, scale, translate);
	}

	/**
	 * Reads an OBJ file and uses the given shader for all triangles. While
	 * loading the triangles they are inserted into the given acceleration
	 * structure accelerator.
	 *
	 * @param in
	 *                    The InputStream of the data to be read.
	 * @param accelerator
	 *                    The target acceleration structure
	 * @param shader
	 *                    The shader which is used by all triangles
	 * @param scale
	 *                    The scale factor which is responsible for scaling the
	 *                    model
	 * @param translate
	 *                    A vector representing the translation coordinate with
	 *                    which
	 *                    all coordinates have to be translated
	 * @throws IllegalArgumentException
	 *                                  If the InputStream is null, the accelerator
	 *                                  is null, the shader is null, the translate
	 *                                  vector is null,
	 *                                  the translate vector is not finite or scale
	 *                                  does not
	 *                                  represent a legal (finite) floating point
	 *                                  number
	 */

	// done with the help of chatgpt, input prompt was accelerator, shader, triangle
	// and project description
	public static void read(final InputStream in,
			final Accelerator accelerator, final Shader shader, final float scale,
			final Vec3 translate) throws FileNotFoundException {
		if (in == null || accelerator == null || shader == null || translate == null || !Float.isFinite(scale)
				|| !translate.isFinite() || Float.isNaN(scale)) {
			throw new IllegalArgumentException("Invalid arguments");
		}

		Scanner scanner = new Scanner(in);
		scanner.useLocale(Locale.ENGLISH);

		List<Point> vertices = new ArrayList<>();

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (line.startsWith("#") || line.isEmpty()) {
				// Ignore comment lines and empty lines
				continue;
			}

			String[] tokens = line.split("\\s+");
			if (tokens[0].equals("v")) {
				// Vertex definition
				float x = Float.parseFloat(tokens[1]);
				float y = Float.parseFloat(tokens[2]);
				float z = Float.parseFloat(tokens[3]);
				Point vertex = new Point(x, y, z).scale(scale).add(translate);
				vertices.add(vertex);
			} else if (tokens[0].equals("f")) {
				// Face definition
				int vertexIndex1 = Integer.parseInt(tokens[1]);
				int vertexIndex2 = Integer.parseInt(tokens[2]);
				int vertexIndex3 = Integer.parseInt(tokens[3]);
				Point vertex1 = vertices.get(vertexIndex1 - 1);
				Point vertex2 = vertices.get(vertexIndex2 - 1);
				Point vertex3 = vertices.get(vertexIndex3 - 1);
				Primitive triangle = GeomFactory.createTriangle(vertex1, vertex2, vertex3);
				accelerator.add(new StandardObj(triangle, shader));
			}
		}

		scanner.close();
	}

}