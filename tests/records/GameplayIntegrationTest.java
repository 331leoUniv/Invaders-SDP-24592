package records;

import engine.*;
import entity.*;
import screen.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;

/** Exercises real collision handlers against the current course gameplay. */
public final class GameplayIntegrationTest {
    private static int checks;
    private static Object get(Object target, String name) throws Exception {
        Field f = field(target.getClass(), name); return f.get(target);
    }
    private static void set(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }
    private static Field field(Class<?> type, String name) throws Exception {
        while (type != null) {
            try { Field f = type.getDeclaredField(name); f.setAccessible(true); return f; }
            catch (NoSuchFieldException e) { type = type.getSuperclass(); }
        }
        throw new NoSuchFieldException(name);
    }
    private static void call(Object target, String name) throws Exception {
        Method m = target.getClass().getDeclaredMethod(name); m.setAccessible(true); m.invoke(target);
    }
    private static void check(boolean value, String message) {
        checks++; if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory(Paths.get("out"), "gameplay-integration-");
        Field global = Core.class.getDeclaredField("recordsService"); global.setAccessible(true);
        Object previous = global.get(null);
        RecordsService service = new RecordsService(directory.resolve("profile.properties"));
        global.set(null, service);
        try {
            for (String path : Arrays.asList("bullet", "beam", "contact")) campaign(service, path);
            infinite(service);
            service.startRun("abandoned"); service.abandonRun("abandoned");
            check(!service.enemyDefeated("abandoned", "late"), "abandoned run rejects late events");
            check(service.getRecordCount() == 0, "gameplay and abandoned runs do not invent completions");
        } finally { global.set(null, previous); service.close(); }
        System.out.println("GameplayIntegrationTest passed: " + checks + " checks.");
    }
    @SuppressWarnings("unchecked")
    private static void campaign(RecordsService service, String path) throws Exception {
        service.startRun(path);
        GameScreen game = new GameScreen(new GameState(7,0,3,0,0),
                new GameSettings(1,1,1000,10000),false,448,520,60,new RunSession(path));
        game.initialize(); set(game,"inputDelay",Core.getCooldown(0));
        Ship player = (Ship)get(game,"ship");
        EnemyShipFormation formation = (EnemyShipFormation)get(game,"enemyShipFormation");
        EnemyShip normal = formation.iterator().next();
        normal.setPositionX(player.getPositionX()); normal.setPositionY(150);
        EnemyShip special = new EnemyShip(); special.setPositionX(player.getPositionX()); special.setPositionY(80);
        set(game,"enemyShipSpecial",special);
        List<String> events = new ArrayList<String>();
        game.getGameplayEvents().subscribe(event.EnemyDefeatedEvent.class, e -> events.add(e.getEventId()));
        if (path.equals("bullet")) {
            Set<Bullet> bullets = (Set<Bullet>)get(game,"bullets");
            bullets.add(BulletPool.getBullet(normal.getPositionX()+4,normal.getPositionY()+4,-4));
            bullets.add(BulletPool.getBullet(special.getPositionX()+4,special.getPositionY()+4,-4));
            call(game,"manageCollisions"); call(game,"manageCollisions");
        } else if (path.equals("beam")) {
            call(game,"manageAnnihilatorBeam"); call(game,"manageAnnihilatorBeam");
        } else {
            player.activateChameleonHull();
            normal.setPositionX(player.getPositionX()); normal.setPositionY(player.getPositionY());
            special.setPositionX(player.getPositionX()); special.setPositionY(player.getPositionY());
            call(game,"manageBallContact"); call(game,"manageBallContact");
        }
        check(events.size()==2, path+" reports exactly two enemy defeats");
        check(new HashSet<String>(events).size()==2, path+" supplies distinct event IDs");
        check(game.getGameState().getShipsDestroyed()==2, path+" preserves upstream kill totals");
        set(game,"levelFinished",true);
        call(game,"manageAnnihilatorBeam"); call(game,"manageCollisions");
        check(events.size()==2,path+" does not count kills after the stage ends");
    }
    @SuppressWarnings("unchecked")
    private static void infinite(RecordsService service) throws Exception {
        service.startRun("infinite");
        InfiniteGameScreen game = new InfiniteGameScreen(new GameState(1,0,3,0,0),448,520,60,new RunSession("infinite"));
        game.initialize();
        List<ZigzagEnemyShip> enemies = (List<ZigzagEnemyShip>)get(game,"enemies");
        Set<Bullet> bullets = (Set<Bullet>)get(game,"bullets");
        List<String> events = new ArrayList<String>();
        game.getGameplayEvents().subscribe(event.EnemyDefeatedEvent.class,e -> events.add(e.getEventId()));
        enemies.add(new ZigzagEnemyShip(100,100,DrawManager.SpriteType.EnemyShipA1,1));
        bullets.add(BulletPool.getBullet(104,104,-4));
        call(game,"manageCollisions"); call(game,"manageCollisions");
        check(events.size()==1 && game.getGameState().getShipsDestroyed()==1,"infinite bullet kill is counted once");
        enemies.add(new ZigzagEnemyShip(100,600,DrawManager.SpriteType.EnemyShipA1,1));
        call(game,"moveEnemies");
        check(events.size()==1,"escaped infinite enemy does not count as defeated");
        Ship player=(Ship)get(game,"ship");
        enemies.add(new ZigzagEnemyShip(player.getPositionX(),player.getPositionY(),DrawManager.SpriteType.EnemyShipA1,1));
        call(game,"manageCollisions");
        check(events.size()==1 && game.getGameState().getLivesRemaining()==2,"player contact loses life without awarding a defeat");
        set(game,"gameOver",true);
        enemies.add(new ZigzagEnemyShip(100,100,DrawManager.SpriteType.EnemyShipA1,1));
        bullets.add(BulletPool.getBullet(104,104,-4)); call(game,"manageCollisions");
        check(events.size()==1,"infinite end animation cannot create more defeats");
    }
}
