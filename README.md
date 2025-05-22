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
