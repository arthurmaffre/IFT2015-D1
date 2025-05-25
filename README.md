# Mini‑tutoriel Git 🔧

Ce guide explique pas à pas comment configurer ton identité et travailler proprement avec des branches.

---

## 1. Cloner le dépôt

```bash
# En local :
git clone https://github.com/arthurmaffre/IFT2015-D1.git
cd IFT2015-D1
```

---

## 2. Configurer ton identité (une seule fois)

```bash
git config --global user.name  "Ton Nom"
git config --global user.email "vous@example.com"
```

> Remplacez *Ton Nom* et *vous@example.com* par tes informations réelles.  

---

## 3. Stratégie de branches

| Branche        | Rôle                                             | Qui la modifie ? |
| -------------- | ------------------------------------------------ | --------------- |
| **main**       | Version 100 % stable / livrable                  | *Personne directement* (merge seulement) |
| **dev**        | Intégration continue, dernière version testée    | Tout le monde via Pull‑Request |
| `feature/...`  | Travail sur une fonctionnalité ou un correctif   | Auteur·rice de la tâche |

---

## 4. Cycle de travail (exemple)

```bash
# Mettre dev à jour
git checkout dev #va de la version ou tu travaille vers dev
git pull origin dev #met a jour depuis le depo la derniere version de dev

# Créer une branche de fonctionnalité
git checkout -b feature/turtle-stack

# … coder, tester …
mvn test

# Committer (à chaque fois que tu publie)
git add src/main/java/lindenmayer/TurtleImpl.java #permet la mise à jour d'un fichier: git add . marche pour ajouter tout mais comme on collabore vas y fichier par fichier
git commit -m "feat: implémentation de la pile d'états dans TurtleImpl" #m c'est le message décrit bien tout ce que tu fais pour que l'on s'y retrouve.

# Pousser
git push -u origin feature/turtle-stack #pousse le commit de ton ordi vers github
```

1. Sur GitHub : ouvrir une Pull‑Request `feature/...` → `dev`.
2. Relecture, puis *Merge*. (si t'es pas à l'aise fait le pull request et je merge stv)
3. Mettre dev à jour en local : (genre si je modifie et que tu veux update dev sur ton ordi depuis github)

```bash
git checkout dev
git pull origin dev
```

---

## 5. Résoudre un conflit

```bash
git pull origin dev        # sur votre branche
# Corriger les <<<<<<< === >>>>>>> dans les fichiers
git add <fichiers>
git commit -m "fix: résolution de conflit"
git push
```

---

## 6. Préparer la livraison finale

1. Vérifier que tout compile (`mvn test`) et que le rapport est prêt.  
2. Ouvrir une PR `dev → main` intitulée « Release v1.0 ».  
3. Tag :

```bash
git checkout main
git pull origin main
git tag -a v1.0 -m "Version finale remise"
git push origin v1.0
```

---

## 7. Bonnes pratiques de commit

- Message court au présent impératif :  
  `feat: add Symbol.rewrite()`  
  `fix: prevent NPE in initFromJson()`
- Pas de fichiers générés (`*.class`, `target/`, etc.) dans les commits.
- Une fonctionnalité = une branche = une PR.

---

## 8. Aide‑mémoire rapide

| Action                                   | Commande |
| ---------------------------------------- | -------- |
| Voir l’historique graphique              | `git log --oneline --graph --decorate` |
| Annuler le dernier commit (local)        | `git reset --soft HEAD~1` |
| Supprimer une branche distante           | `git push origin --delete feature/...` |
| Lister branches (locales + distantes)    | `git branch -a` |
| Rebaser votre branche sur dev            | `git fetch origin && git rebase origin/dev` |

---

Avec ce tutoriel, même un•e débutant•e peut contribuer sans risque au projet.



# Projet 1 : Comment dessiner un arbre

## 1. Membres de l'équipe
- **Étudiant·e 1** : Maffre Arthur
- **Étudiant·e 2** : Lowyn Van Waerbeke

## 2. Organisation de travail
1. **Rencontres et coordination** :
   - Pair programming et points de synchronisation hebdomadaires
   - Outils de communication : Slack
2. **Gestion de code** :
   - Référentiel Git hébergé sur GitHub
   - Branches : `main` pour la version stable, `dev` pour le développement
3. **Répartition des tâches** :
   - Tortue (`TurtleImpl`) et gestion de la pile d’états : Arthur
   - Moteur L-système (init, rewrite, tell) : Lowyn
   - Lecture JSON et initialisation (`initFromJson`) : Arthur
   - Application principale et sortie EPS : Lowyn
4. **Résolution de conflits** :
   - Revue de code croisée et vote à la majorité simple

## 3. Fonctionnalités à implémenter

### a. Tortue à crayon imaginaire
- Classe implémentant l’interface `Turtle`
- Gestion de l’état (position, angle) et de la pile (`push`/`pop`)
- Conversion degrés ↔ radians pour déplacements et rotations

### b. Moteur L-système
- Classes `Symbol` et `LSystem`
- Méthodes :
  - `setAction(char, String)`
  - `setAxiom(String)`
  - `addRule(Symbol, String)`
  - `getAxiom()` et `rewrite(Symbol)`
  - `tell(Turtle, Symbol)` pour exécuter une instruction unique

### c. Initialisation par fichier JSON
- Méthode `initFromJson(JSONObject obj, Turtle turtle)`
- Lecture des sections : `actions`, `rules`, `axiom`, `parameters`
- Configuration de la tortue (`step`, `angle`, `start`)

### d. Inférence et dessin récursif
- Méthode `Rectangle2D tell(Turtle, Iterator<Symbol>, int rounds)`
- Algorithme récursif sans construire la chaîne complète
- Calcul du bounding box (union de rectangles)

### e. Application principale (EPS)
- Classe exécutable `lindenmayer.jar`
- Arguments CLI : `<fichier.json> <n-iterations>`
- Génération du code EPS sur `System.out`
  - En-tête EPS, commandes PostScript (`moveto`, `lineto`, `stroke`)
  - Gestion du chemin et marquage du bounding box

## 4. Bonus (2 points)

### f. Implémentation en PostScript pure
- Inference des chaînes dans le code PostScript
- Utilisation de `gsave`/`grestore`, `translate`, `rotate`

### g. Affichage alternatif (JavaFX, Swing ou SVG)
- Client graphique basé sur `GraphicsContext` ou génération de `<path>` SVG

## 5. Rapport
- Format : PDF ou texte (1–2 pages)
- Contenu :
  1. Membres de l’équipe et rôle de chacun
  2. Organisation de travail et communication
  3. Description de l’implémentation et choix techniques
  4. Bilan du temps passé (heures de codage vs. total)
  5. Difficultés rencontrées et résolutions

## 6. Compilation et exécution
```bash
# Compilation (exemple Maven)
mvn package

# Exécution
java -jar target/lindenmayer.jar spec/lindenmayer.json 5 > arbre5.eps
```

## 7. Structure du dépôt
```
README.md
src/main/java/lindenmayer/
  ├── Symbol.java
  ├── Turtle.java
  ├── AbstractLSystem.java
  ├── LSystem.java
  ├── TurtleImpl.java
  └── Main.java
spec/
  └── example.json
report/
  └── rapport.pdf
```

---
**Bonne implémentation !**
