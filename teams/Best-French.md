# Team Document: Best French
**Course:** CSE2024 Software Development Practices  
**Section:** 24592 (Afternoon)  
**Assigned Subsystem:** Requirement 5 - Item System  

---

## 1. Team Introduction

Our ambition is to design and implement a dynamic, robust, and extensible Item System for Space Invaders SDP. The vision is to enhance gameplay depth and player engagement by introducing collectible items, power-ups, and inventory mechanics that seamlessly interact with combat and progression systems.

To ensure consistent, balanced, and verifiable individual contributions throughout the semester, engineering responsibilities are distributed so that each team member actively designs, implements, and tests dedicated components.

### Members & Responsibilities

| Name | Role & Primary Responsibility | Contribution Scope & Code Ownership | GitHub Profile |
| :--- | :--- | :--- | :--- |
| **Leo Enaux** | Team Leader & Core Architecture | Core ItemManager, entity lifecycle loop, spawning factory, and upstream integration | [@leoenaux](https://github.com/leoenaux) |
| **Mathéo SUHR** | Physics & Collision Developer | Drop velocity, boundary checks, player/item bounding box collisions, and drop tables | [@Matheo92i](https://github.com/Matheo92i) |
| **Clarisse Majourau** | Gameplay Developer (Twin Cannon) | Dual projectile trajectory math, duration tracking, and firing pattern swapping | [@clarissemajourau](https://github.com/clarissemajourau) |
| **Brieuc VIOUGEAT** | Gameplay Developer (Chameleon Hull) | Color-cycling shader/sprite routine, player agility modifier, and timer loops | [@BrieucVio](https://github.com/BrieucVio) |
| **Raphaelle Amar** | Gameplay Developer (Cluster Grenade) | Secondary ordnance projectile entity, detonation altitude check, and radial AoE logic | [@Raphaelle94](https://github.com/Raphaelle94) |
| **Inès Safir** | Gameplay Developer (Hyperfire Buff) | Ship cooldown reduction calculations, active state lifecycle, and HUD gauge sync | [@ines-saphir](https://github.com/ines-saphir) |
| **Manel Belfedhal** | Gameplay Developer (Orbital Annihilator) | Penetrating laser beam rendering, 1.5s charge lock, and mass enemy/bullet erasure | [@belfedhal-manel](https://github.com/belfedhal-manel) |
| **Aurele Ouary** | Audio & Visual FX Integrator | Floating item sprites, buff aura animations, audio event triggers, and unit testing suites | [@aureleouaryPro](https://github.com/aureleouaryPro) |

---

## 2. Team Requirements

We are developing the **Item System** (Requirement 5) of the Space Invaders project. This module manages the generation, physics, collision detection, and gameplay impact of interactive pickups. It acts as the backbone for temporary combat buffs, active sub-weapons, and consumable tactical advantages.

---

## 3. Detailed Requirements (Item Behaviors)

1. **Twin Cannon Upgrade (Double Barrel)**
   * **Behavior:** Spawns dynamically midway through Level 2 upon destroying a designated wave milestone. Once collected by the ship, it upgrades the standard single-projectile fire into a dual-stream parallel volley for 25 seconds. If the player gets hit and loses a life during this window, the buff expires immediately.

2. **Chameleon Hull (Color-Shifter)**
   * **Behavior:** Unlocks and spawns during Level 3. Upon collection, it causes the player ship's sprite to cycle continuously through a dynamic palette of colors every 2 seconds. While active, it grants a passive 10% movement speed increase and reduces alien targeting accuracy.

3. **Cluster Grenade Launcher**
   * **Behavior:** Equips the ship with an explosive ordnance sub-weapon mapped to an alternate fire action. Launches a slow-moving grenade that detonates either upon colliding with an enemy or reaching 70% screen altitude. Upon detonation, it fragments into 6 radial projectiles dealing localized Area-of-Effect (AoE) splash damage to nearby enemy clusters.

4. **Overclocked Fire Rate (Hyperfire)**
   * **Behavior:** A high-intensity combat pickup that temporarily decreases the player ship's weapon firing cooldown delay by 60% (from 400ms to 160ms between shots) for a duration of 10 seconds, enabling rapid-fire suppressive bursts against advancing formations.

5. **Orbital Annihilator (Doomsday Laser)**
   * **Behavior:** A rare, high-tier ultimate item introduced in Level 7. When triggered, it initiates a 1.5-second charging phase (with partial movement lock and a visual targeting reticle), followed by a full-screen vertical piercing energy beam lasting 2 seconds that completely vaporizes all standard enemy ranks and neutralizes incoming enemy projectiles in its corridor.

---

## 4. Dependencies on Other Teams

1. **Player & Enemy Ship Variety (Requirement 9):**
   * *Dependency:* The Twin Cannon, Cluster Grenade, and Hyperfire items directly modify ship attributes, firing intervals, and projectile spawn offsets inside `PlayerShip.java`.

2. **Gameplay HUD (Requirement 8):**
   * *Dependency:* Active temporary item buffs (such as the 10-second Hyperfire cooldown buff and the 25-second Twin Cannon timer) require dedicated on-screen countdown gauges or status icons drawn on the player HUD.

3. **Sound Effects / BGM (Requirement 1):**
   * *Dependency:* Dedicated audio cues and SFX hooks are required for item pickup sounds, firing sound variations, grenade explosions, and the charging beam of the Orbital Annihilator.
