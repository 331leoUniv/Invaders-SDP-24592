# Team Document: Best French

## 1. Team Introduction
Our ambition is to design and implement a dynamic, robust, and extensible Item System for Space Invaders SDP. The vision is to enhance gameplay depth and player engagement by introducing collectible items, power-ups, and inventory mechanics that seamlessly interact with combat and progression systems.

### Members & Roles

| Name | Role | GitHub Profile |
| :--- | :--- | :--- |
| **Leo Enaux** | Team Leader & Integration Manager | [@leoenaux](https://github.com/leoenaux) |
| **Mathéo SUHR** | Core Logic Developer (Collision & Spawning) | [@Matheo92i](https://github.com/Matheo92i) |
| **Clarisse Majourau** | Developer (Twin Cannon Implementation) | [@clarissemajourau](https://github.com/clarissemajourau) |
| **Brieuc Viougeat** | Developer (Chameleon Hull Logic) | [@BrieucVio](https://github.com/BrieucVio) |
| **Raphaelle Amar** | Developer (Cluster Grenade Mechanic) | [@Raphaelle94](https://github.com/Raphaelle94) |
| **Inès Safir** | Developer (Hyperfire Buff System) | [@ines-saphir](https://github.com/ines-saphir) |
| **Manel Belfedhal** | Developer (Orbital Annihilator Laser) | [@belfedhal-manel](https://github.com/belfedhal-manel) |
| **Aurele Ouary** | QA & Test Engineer (Unit Tests & Validation) | [@aureleouaryPro](https://github.com/aureleouaryPro) |

---

## 2. Team Requirements
We are developing the **Item System** (Requirement 5) of the Space Invaders game. This module manages the lifecycle, attributes, spawning logic, and effects of all in-game items. It acts as the backbone for player enhancements and consumable rewards during gameplay.

---

## 3. Detailed Requirements (Item Behaviors)

1. **Twin Cannon Upgrade (Double Barrel)**
   * **Behavior:** Spawns dynamically midway through Level 2 upon clearing a specific wave threshold. When collected by the player ship, it upgrades the standard single-projectile fire into a dual-stream parallel volley for 25 seconds. If the player is hit and loses a life during this period, the upgrade is immediately lost.

2. **Chameleon Hull (Color-Shifter)**
   * **Behavior:** Unlocks and spawns during Level 3. Upon collection, it causes the player's ship sprite to cycle continuously through a dynamic palette of colors every 2 seconds. While active, it grants a passive 10% movement speed increase and reduces alien targeting accuracy.

3. **Cluster Grenade Launcher**
   * **Behavior:** Equips the player ship with an explosive ordnance sub-weapon. Launches slow-moving grenades that detonate either upon direct collision with an enemy or upon reaching a fixed screen altitude. Upon detonation, it splits into 6 radial fragments dealing localized Area-of-Effect (AoE) splash damage to nearby alien formations.

4. **Overclocked Fire Rate (Hyperfire)**
   * **Behavior:** A high-intensity combat pickup that temporarily decreases the player ship's weapon cooldown delay by 60% (from 400ms to 160ms between shots) for a duration of 10 seconds. This enables rapid-fire bursts to suppress fast-advancing enemy lines.

5. **Orbital Annihilator (Doomsday Laser)**
   * **Behavior:** A rare, high-tier ultimate item introduced in Level 7. When triggered, it initiates a 1.5-second charging phase followed by a full-screen vertical piercing energy beam lasting 2 seconds that vaporizes all standard enemy ranks and neutralizes incoming enemy projectiles within its corridor.

---

## 4. Dependencies on Other Teams

1. **Player & Enemy Ship Variety (Requirement 9):**
   * *Dependency:* The Twin Cannon, Cluster Grenade, and Hyperfire items directly modify the player ship's weapon configurations, shooting cooldown timers, and projectile spawn sockets located in `Ship.java` / `PlayerShip.java`.

2. **Gameplay HUD (Requirement 8):**
   * *Dependency:* Active temporary item buffs (such as the 10-second Hyperfire cooldown buff and the 25-second Twin Cannon timer) require visual timers, gauges, or status icons drawn on the player HUD.

3. **Sound Effects / BGM (Requirement 1):**
   * *Dependency:* Audio triggers and sound clips are required for item pickup notifications, weapon change sound effects, grenade blast detonation, and the charging beam of the Orbital Annihilator.
