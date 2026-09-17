# Team HanCode

## 1. Team Introduction

**Requirements:** 4. Currency System

**Course:** CSE2024 Software Development Practices

### Goals and Vision:

Our goal is to develop a robust, bug-free, and scalable currency system that feels rewarding, enhances the player's progression and seamlessly integrates with other gameplay mechanics.The system should integrate smoothly with other gameplay systems such as the HUD, item system, and enemy variety system.

### Members:

| **Member** | **Role** | **GitHub** |
| :--- | :--- | :--- |
| 오세윤 | PM / TeamLeader | https://github.com/ogaji |
| Khuvituguldur | Developer | https://github.com/tuugy-rvn |
| Isaac de Jesus Rojas Torres | Developer | https://github.com/isaacrt54 |
| Joshua Hernández Ruiz | Developer | https://github.com/Jperf0 |
| Anukhishig | Documentation | https://github.com/Anukhishig |
| Byambakhishig Khishigjin | QA Tester | https://github.com/hishigjinb-svg |
| 제현승 | Dev Lead / Collaborator | https://github.com/HyunseungJe |
| 여민경 | Documentation | https://github.com/yeominkyung |

---

## 2. Team Requirements

### Overall Requirement:

Currency System. Our team is responsible for managing the logic, balance, and persistence of the in-game currency earned by players during gameplay.

---

## 3. Detailed Requirements

1. Implement a core `CurrencyManager` class to handle the secure addition, subtraction, and tracking of in-game currency.
2. Define and implement logic for currency drop rates and reward amounts based on the destruction of specific enemy ship types or completing levels.
3. Implement a data persistence mechanism to save and load the player's currency balance between different game sessions.
4. Create an API/interface that allows other systems to check the current balance and deduct currency when a player makes a purchase.
5. Develop error-handling logic to prevent invalid transactions (e.g., spending more currency than the player currently owns).

---

## 4. Dependencies on Other Teams

1. **Gameplay HUD:** We depend on the HUD team to fetch and continuously display the current currency balance to the player on the screen.
2. **Item System:** We depend on the Item System to trigger our purchase methods when a player attempts to buy an item or upgrade.
3. **Player & Enemy Ship Variety:** We depend on the enemy destruction events to trigger our currency reward logic when an alien is defeated.
