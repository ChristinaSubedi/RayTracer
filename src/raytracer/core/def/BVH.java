package raytracer.core.def;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import raytracer.core.Hit;
import raytracer.core.Obj;
import raytracer.geom.BBox;
import raytracer.math.Point;
import raytracer.math.Ray;
import raytracer.math.Vec3;

//Whole class was done with the help of chatgpt, first gave it everything, ie project description, all the required classes and the empty skeleton and asked it to fill it, then corrected each class one by one

/**
 * Represents a bounding volume hierarchy acceleration structure
 */
public class BVH extends BVHBase {
    private BBox bbox;
    private List<Obj> objects;

    public BVH() {
        bbox = BBox.EMPTY;
        objects = new ArrayList<>();
    }

    @Override
    public BBox bbox() {
        return bbox;
    }

    /**
     * Adds an object to the acceleration structure
     *
     * @param prim
     *             The object to add
     */
    @Override
    public void add(final Obj prim) {
        objects.add(prim);
        bbox = BBox.surround(bbox, prim.bbox());
    }

    // done with chatgpt, gave project description, skeleton, the rest of the class
    // bvhbase, and bbox as input

    /**
     * Builds the actual bounding volume hierarchy
     */
    @Override
    public void buildBVH() {
        if (objects.size() <= 4) {
            // Stop splitting when there are 4 or fewer objects
            return;
        }

        // Calculate the maximum of the minimum points of all contained objects
        Point maxOfMinPoints = calculateMaxOfMinPoints();

        // Determine the dimension to split
        Vec3 extent = bbox.getMax().sub(maxOfMinPoints);
        int splitDim = calculateSplitDimension(extent);

        // Calculate the split position
        float splitPos = (bbox.getMin().get(splitDim) + bbox.getMax().get(splitDim)) / 2.0f;

        // Create sub-boxes
        BVHBase left = new BVH();
        BVHBase right = new BVH();

        // Distribute objects into sub-boxes
        distributeObjects(left, right, splitDim, splitPos);

        // Build sub-boxes recursively
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> left.buildBVH());
        executor.submit(() -> right.buildBVH());
        executor.shutdown();

        try {
            // Wait for both sub-boxes to finish building
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Clear the current objects list and add the sub-boxes
        objects.clear();
        objects.add(left);
        objects.add(right);

        // Update the bounding box
        bbox = BBox.surround(left.bbox(), right.bbox());
    }

    // done with chatgpt, gave project description and bbox

    @Override
    public Point calculateMaxOfMinPoints() {
        Point maxPoint;

        maxPoint = objects.get(0).bbox().getMin();

        for (int i = 1; i < objects.size(); i++) {
            BBox objBBox = objects.get(i).bbox();
            maxPoint = maxPoint.max(objBBox.getMin());
        }

        return maxPoint;
    }

    // done with chatgpt, gave it project description, and vectors class and the
    // rest of this class
    @Override
    public int calculateSplitDimension(final Vec3 extent) {
        if (extent.x() > extent.y() && extent.x() > extent.z()) {
            return 0; // Split along the X-axis
        } else if (extent.y() > extent.z()) {
            return 1; // Split along the Y-axis
        } else {
            return 2; // Split along the Z-axis
        }
    }

    // done with chatgpt, gave it project description, and vectors class and the
    // rest of this class, obj class
    @Override
    public void distributeObjects(final BVHBase a, final BVHBase b,
            final int splitDim, final float splitPos) {

        for (Obj obj : objects) {
            BBox objBBox = obj.bbox();
            float objMin = objBBox.getMin().get(splitDim);

            if (objMin <= splitPos) {
                a.add(obj);
            } else {
                b.add(obj);
            }
        }

    }

    // done with chatgpt, gave it project description, and hit class and the
    // rest of this class and bvb class and hit class

    @Override
    public Hit hit(final Ray ray, final Obj obj, final float tMin, final float tMax) {
        if (!bbox.hit(ray, tMin, tMax).hits())
            return Hit.No.get();

        Hit closestHit = Hit.No.get();

        for (Obj child : objects) {
            Hit hit = child.hit(ray, obj, tMin, tMax);
            if (hit.hits() && hit.getParameter() < closestHit.getParameter()) {
                closestHit = hit;
            }
        }

        return closestHit;
    }

    @Override
    public List<Obj> getObjects() {
        return objects;
    }
}
