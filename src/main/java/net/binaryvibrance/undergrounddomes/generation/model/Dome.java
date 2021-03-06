package net.binaryvibrance.undergrounddomes.generation.model;

import net.binaryvibrance.helpers.maths.GeometryHelper;
import net.binaryvibrance.helpers.maths.Line;
import net.binaryvibrance.helpers.maths.Point3D;
import net.binaryvibrance.undergrounddomes.generation.contracts.ILineIntersectable;

import java.util.LinkedList;
import java.util.List;

public class Dome implements ILineIntersectable {
    public DomePurpose purpose;
    private List<DomeFloor> domeFloors = new LinkedList<DomeFloor>();
    private Point3D location;
    private int diameter;

    public Dome(Point3D location, int diameter) {
        this.diameter = diameter;
        this.location = location;
    }

    public Point3D getLocation() {
        return location;
    }

    public double getRadius() {
        return diameter / 2;
    }

    public int getDiameter() {
        return diameter;
    }

    public void addFloor(DomeFloor floor) {
        domeFloors.add(floor);
    }

    public DomeFloor getFloor(int floor) {
        return domeFloors.get(floor);
    }

	public boolean isFloorLevel (int level) {
		for (DomeFloor floor : domeFloors) {
			if (floor.getLevel() == level) {
				return true;
			}
		}
		return false;
	}

    @Override
    public boolean intersects(Line line) {
        return GeometryHelper.lineIntersectsSphere(line, location, getRadius());
    }

	@Override
	public String toString() {
		return String.format("Dome (%s Diameter %d)", location, diameter);
	}

	public DomeFloor[] getFloors() {
		return domeFloors.toArray(new DomeFloor[domeFloors.size()]);
	}
}
