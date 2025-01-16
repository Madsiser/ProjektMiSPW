package pl.sim.backend;

import pl.simNG.*;

import java.util.Random;

public class BaseGroup extends SimGroup {

    Random random = new Random();
    private SimPosition originalDestination;

    public BaseGroup(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);

        SimUnit unit = new UnitManager.Abrams(5);
        this.addUnit(unit);
    }

    @Override
    public void init(){
        originalDestination = new SimPosition(2, 2);
        this.route = calculateRouteTo(new SimPosition(2,2));
        addTask(this::move,1);
        addProcess("shot", this::shot, 5);
    }

    @Override
    public void move() {
        int groupSpeed = getSpeed();
        double stepSize = 0.1 * groupSpeed; // Skala szybkości dla płynności

        if (!visibleGroups.isEmpty()) {
            SimGroup target = visibleGroups.get(0);
            if (units.stream().anyMatch(unit -> unit.inShotRange(target.getPosition()))) {
                shot();
            } else {
                attackTarget(target.getPosition(), stepSize);
            }
        } else {
            if (route.isEmpty() && !(this.position.getX() == originalDestination.getX() && this.position.getY() == originalDestination.getY())) {
                System.out.println(getName() + " oblicza trasę do pierwotnego celu");
                this.route = calculateRouteTo(originalDestination);
            }
            if (!route.isEmpty()) {
                SimVector2i direction = route.poll();
                if (direction != null) {
                    SimVector2d smoothDirection = new SimVector2d(direction.getX(), direction.getY()).scale(stepSize);
                    System.out.println(getName() + " kontynuuje ruch po trasie");
                    this.position.add(smoothDirection.getDx(), smoothDirection.getDy());
                }
            }
        }

        addTask(this::move, 1); // Nadal zachowujemy opóźnienie dla płynności
    }

    public void apply_damage(SimGroup attacker) {
        if (!units.isEmpty()) {
            SimUnit unit = units.get(0);
            unit.setActiveUnits(unit.getActiveUnits()-1);
            this.cleanDestroyedUnits();
        }
    }

    public void shot(){
        if(!visibleGroups.isEmpty()){
            SimGroup target = visibleGroups.get(0);
            for(SimUnit unit: units){
                if (unit.inShotRange(target.getPosition())){
                    target.apply_damage(this);
                    System.out.println(getName() + " strzela do grupy: " + target.getName());
                }
            }
        }
    }

    private void attackTarget(SimPosition targetPosition, double speed) {
        System.out.println(getName() + " oblicza trasę do celu");
        this.route = calculateRouteTo(targetPosition);

        if (!route.isEmpty()) {
            SimVector2i direction = route.poll();
            if (direction != null) {
                System.out.println(getName() + " porusza się w kierunku celu");
                this.position.add(direction);
            }
        }
    }
}
