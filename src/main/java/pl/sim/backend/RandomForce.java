package pl.sim.backend;

import pl.simNG.*;

import java.util.LinkedList;
import java.util.Random;

public class RandomForce extends SimGroup {

    Random random = new Random();
    private LinkedList<SimVector2i> originalRoute = null;

    public RandomForce(String name, SimPosition position, SimForceType forceType) {
        super(name, position, forceType);


        SimUnit unit = new Abrams(5);
        this.addUnit(unit);

//        List<SimVector2i> directions = new ArrayList<>();
//        directions.add(SimVector2i.UP);
//        directions.add(SimVector2i.DOWN);
//        directions.add(SimVector2i.LEFT);
//        directions.add(SimVector2i.RIGHT);

//        int length = 1000;
//        for (int i = 0; i < length; i++) {
//            SimVector2i randomDirection = directions.get(random.nextInt(directions.size()));
//            this.route.add(randomDirection);
//        }

    }

    @Override
    public void init(){
        this.route = calculateRouteTo(new SimPosition(2,2));

        addTask(this::move,1);
        addProcess("shot", this::shot, 5);
    }

    @Override
    public void move() {
            if (!visibleGroups.isEmpty()) {
                SimGroup target = visibleGroups.get(0);
                if (isInShootingRange(target)) {
                    System.out.println(getName() + " strzela");
                    shot();
                } else {
                    moveToTarget(target.getPosition());
                }
            } else {
                if (originalRoute != null) {
                    System.out.println(getName() + " przywraca pierwotną trasę");
                    this.route = originalRoute;
                    originalRoute = null;
                }

                if (!route.isEmpty()) {
                    SimVector2i direction = route.poll();
                    if (direction != null) {
                        System.out.println(getName() + " kontynuuje ruch po trasie");
                        this.position.add(direction);
                    }
                }
            }
        addTask(this::move, 1);
    }

    @Override
    public void apply_damage(SimGroup attacker, SimBullet bullet) {
        if (!units.isEmpty()) {
            SimUnit unit = units.get(0);
            unit.setAmount(unit.getAmount()-1);
            units.set(0, unit);
            this.cleanDestroyedUnits();
        }
    }

    public void shot(){
        if(!visibleGroups.isEmpty()){
            SimGroup target = visibleGroups.get(0);
            for(SimUnit unit: units){
                if (unit.inShotRange(target.getPosition())){
                    target.apply_damage(this, new TankShell());
                    System.out.println(getName() + " strzela do grupy: " + target.getName());
                }
            }
        }
    }

    private boolean isInShootingRange(SimGroup target) {
        return this.position.distanceTo(target.getPosition()) <= units.stream()
                .mapToInt(SimUnit::getShotRange)
                .max()
                .orElse(0);
    }

    private void moveToTarget(SimPosition targetPosition) {
        if (originalRoute == null) {
            System.out.println(getName() + " zapisuje pierwotną trasę");
            originalRoute = new LinkedList<>(route);
        }

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
