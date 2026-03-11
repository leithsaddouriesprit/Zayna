# Manual test scenarios – AI Assistant (no “test mode” in UX)

## 1) Parent has children + tracking exists → immediate real answer

**Setup:** Parent logged in, at least one active child with a trajet and bus; `position_bus` has at least one row for that bus.

**Steps:**
1. Open the AI Assistant (Ask Zayna AI) from Parent home.
2. Wait for the welcome line: *"Je suis prêt. Je suis en train de suivre [Prénom Nom] ([trajet ou bus]). Pose ta question."*
3. Send **"bonjour"**.

**Expected:** Reply like: *"Je suis prêt. Je suis en train de suivre [Nom] (bus [matricule]). Pose ta question. Dernière mise à jour: … Position bus: lat, lng. ETA prochain arrêt: ~X min."* (details if tracking is present). No "mode test".

4. Ask **"Mon enfant est dans le bus ?"** or **"Où est le bus ?"**.

**Expected:** Answer based on real data (on_board, position, ETA). No hallucination, no "mode test".

---

## 2) Parent has children but no tracking → explain tracking not available (no hallucination)

**Setup:** Parent has at least one child with trajet/bus, but no rows in `position_bus` for that bus (or bus has no position yet).

**Steps:**
1. Open the AI Assistant.
2. Welcome line should still show: *"Je suis prêt. Je suis en train de suivre [Nom] (…). Pose ta question."*
3. Send **"bonjour"**.

**Expected:** *"Je suis prêt. Je suis en train de suivre [Nom] (…). Pose ta question."* (no position/ETA if none). No "mode test".

4. Ask **"Où est mon enfant ? position exacte"**.

**Expected:** MISSING_CONTEXT or clear message that position is not available, e.g. *"La position du bus n'est pas disponible. Activez le suivi GPS ou réessayez plus tard."* with suggested actions. No invented coordinates, no "mode test".

---

## 3) Parent has no children → ask to add/select a child

**Setup:** Parent logged in with no active children (empty list from `enfant` for that `parent_id`), or open chat without having selected an child and with no default.

**Steps:**
1. Open the AI Assistant (without a selected child and with no children in DB for that parent).

**Expected (JavaFX):** Welcome: *"Aucun enfant associé. Sélectionnez ou ajoutez un enfant depuis l'accueil pour utiliser l'assistant."* Input/send disabled or discouraged.

**If a request is still sent (e.g. from another client):** API returns MISSING_CONTEXT: *"Aucun enfant associé à votre compte. Sélectionnez ou ajoutez un enfant pour utiliser l'assistant."* with suggested_actions: "Sélectionnez un enfant", "Ou ajoutez un enfant depuis l'accueil". No "mode test".

---

## Bonus – short greeting (UX)

After auto-context loads, the first line must always be short and reassuring:

- *"Je suis prêt. Je suis en train de suivre [Prénom Nom] ([trajet ou bus]). Pose ta question."*

This confirms which child is followed by default and that the assistant is ready.
