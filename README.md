# Lattice_Gas_Automata
The project implements easier and simpler way to visualize diffusion than LBM
## Description of the technologies used
I wrote the entire program in Java because this language includes a number of facilities for creating the graphical interface needed to visualize the simulation. Compared to other languages, creating a GUI in Java is rather simple. Compared to the QT framework for C++, it is much simpler and contains a number of useful classes that can be used without struggling with the drawbacks of C++. I used the JavaFX and Swing libraries to create visualizations. As I have already mentioned, the process of creating a UI is quite simple, in the main() class of my program, I create an object of the MainUI class, so the constructor of this class works.  
```java
import view.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main (String[] argv) throws InterruptedException, IOException {
        MainUI ui = new MainUI();

        while (true) {
            while (!ui.ifClicked()) {
                TimeUnit.MILLISECONDS.sleep(1000);
            }
            ui.simulate();
        }
    }
}
```
It initializes the elements responsible for the graphical interface.  
I created the project in the <b>MVC</b> architecture (model-view-controller), so apart from the classes responsible for the graphical interface, there are also classes responsible for creating what objects look like (classes) and how they behave (interfaces).  
Due to the multitude of amenities and friendly user interface, I used the IntelliJ IDEA Community Edition 2021.3.3 IDE as the development environment.  
I used the Java SDK version 18.0.1.1, but I did not use the facilities provided by newer versions (from Java 8) of Java. The only functionality is the use of the var keyword, especially when creating and initializing instances of classes whose type is obvious due to the constructor being called during assignment, as in the listing below:  
```java
var simulateButton = new JButton("Start / stop");
```
Moreover, I also used this keyword in the for loop. Entering var instead of a keyword makes things much easier when I change the type of collection I'm iterating over.
```java
for (var elem : cells) {
    if (elem.size() < 400) {
        System.out.println(elem.size());
    }
}
```
I don't use many external libraries except for UI development. However, I use certain libraries I wrote.  
The first of them is <b>ImagePanel</b>, a class that inherits from the JPanel class. It allows for a very simple modification of the image or graphics currently displayed in the UI. Contains methods for changing the image and overwrites the method responsible for refreshing the image.  
```java
package model;
import controller.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {

    private BufferedImage image;

    public ImagePanel() {
        image = (new FileUtil()).openFromFile();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this);
    }
}
```
The above class makes generous use of methods provided by a second class that I often use in projects - <b>FileUtil</b>. It, in turn, contains only two methods: for writing and reading from a graphic file.
```java
package controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class FileUtil {
    public void saveToFile(BufferedImage img) throws IOException {
        ImageIO.write(img, "bmp",
                new File("src\\input.png"));
    }

    public BufferedImage openFromFile() {
        try {
            return ImageIO.read(new File("src\\input.png"));
        } catch (IOException ex) {
            System.exit(-1);
            return null;
        }
    }
}

```
## Description of the model and implementation
In my project, each cell is represented as an instance of the Cell class, the contents of which are presented in the listing below.
```java
package model;

import java.util.ArrayList;
import java.util.Collections;

// class represents one cell
public class Cell {
    // state is an arraylist of size 4, informs whether a particle is flowing
    // value 1 on 0th position means a particle coming to the upper cell
    // value 1 on 1st position means a particle coming to the right cell
    // value 1 on 2nd position means a particle coming to the down cell
    // value 1 on 3rd position means a particle coming to the left cell
    ArrayList<Integer> state;
    // type represents an info whether this cell is BORDER (black) or not
    final cellType type;

    public Cell(cellType type) {
        state = new ArrayList<>(Collections.nCopies(4, 0));
        this.type = type;
    }

    public ArrayList<Integer> getState() {
        return state;
    }

    public void setState(ArrayList<Integer> state) {
        this.state = state;
    }

    public cellType getType() {
        return type;
    }

    public enum cellType{
        BORDER,
        NORMAL
    }
}
```
Each cell has a state list, a 4-element list that informs whether particle are coming toany of the four sides (which is why the size of the list is four). In addition to this information, I also have an enum that denotes the type of cell - whether it is a wall or not. When it is a wall, the way of analyzing the cells in the vicinity of the wall cell is significantly different, but more on that later.  
I added getters and setters to the fields of the Cell class.  
Note: The cell type, whether it is a wall or not, is determined once during map analysis. Therefore, it is worth using the final keyword to prevent accidental (rather than intentional) modifications of the cell type. Indeed, a cell that is a wall cannot become a non-wall at the moment of program execution. Conversely, a normal cell cannot become a wall. Therefore, this attribute does not have a setter.  
The entire control was carried out in the CAUtil class. There was a 2D list here that corresponded to each cell (and also a pixel) and stored information about the cells.
```java
private ArrayList<ArrayList<Cell>> cells;
```
In the class constructor (which, by definition, was called once - when creating an instance of the class), I initialized the array and also called two methods - one created particles, and the other recognized where there were walls and where not based on the initial map.  
```java
// one-time board initialization
public CAUtil(BufferedImage img) throws IOException {
   cells = new ArrayList<>(img.getHeight());
   for (int i = 0; i < img.getWidth(); i++) {
       cells.add(new ArrayList<>());
   }
   this.image = img;

    generateParticles();
   guessType();
 }
```
I randomly selected two things: the first was the place where the particle was and the second was the initial state - each cell could have information about the direction in which the particle was moving.
```java
Random rand = new Random();
ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
newState.set(rand.nextInt(4), 1);
```
The lines above on a random index, insert one.  
When it comes to the execution of the program itself, I iterate over all the elements. If a cell is a wall, it is still a wall.  
```java
Random rand = new Random();
int tmp = rand.nextInt(4) + 1;
ArrayList<Integer> newState = switch (tmp) {
    case 1 -> new ArrayList<>(Arrays.asList(1, 0, 0, 0));
    case 2 -> new ArrayList<>(Arrays.asList(0, 1, 0, 0));
    case 3 -> new ArrayList<>(Arrays.asList(0, 0, 1, 0));
    case 4 -> new ArrayList<>(Arrays.asList(0, 0, 0, 1));
    default -> new ArrayList<>(Arrays.asList(0, 0, 0, 0));
};
```
Now I create secondary instances of the Cell class that correspond to the cells at the top, bottom, right, and left. This is not necessary, but it simplifies code analysis.
```java
Cell cellUp = this.cells.get(i-1).get(j);
Cell cellDown = this.cells.get(i+1).get(j);
Cell cellRight = this.cells.get(i).get(j+1);
Cell cellLeft = this.cells.get(i).get(j-1);
```
Then I check if there is a wall cell next to it. If so, and the particle is supposed to move there, it is <b>reflected</b> from the wall.
```java
 if (state.get(0).equals(1) && cellUp.getType().equals(Cell.cellType.BORDER)) { // upper cell is border
     newState.set(2, 1);
  } else if (state.get(1).equals(1) && cellRight.getType().equals(Cell.cellType.BORDER)) { // right cell is border
     newState.set(3, 1);
   } else if (state.get(2).equals(1) && cellDown.getType().equals(Cell.cellType.BORDER)) { // bottom cell is border
      newState.set(0, 1);
  } else if (state.get(3).equals(1) && cellLeft.getType().equals(Cell.cellType.BORDER)) { // left cell is border
     newState.set(1, 1);
```
Then I implement the <b>streaming</b> operation. If two cells next to me (top, bottom, left, right) have a particle and it is to enter my cell, then in the next step it will leave my cell to the next one. Therefore (depending on where the particle comes from) I set the state information in my current cell and reset the state to zero in the cell where the particle came from (because that's where it came from). These four blocks only differ in where the particle goes from and to.
```java
// streaming - if new particle come
if (cellUp.getState().get(2).equals(1)) {
     newState.set(2, 1);
}
if (cellRight.getState().get(3).equals(1)) {
    newState.set(3, 1);
}
if (cellDown.getState().get(0).equals(1)) {
   newState.set(0, 1);
}
if (cellLeft.getState().get(1).equals(1)) {
  newState.set(1, 1);
}
```
The last operation is the <b>collision</b> operation. It only happens in two cases:
*	(1, 0, 1, 0) ---> (0, 1, 0, 1)
*	(0, 1, 0, 1) ---> (1, 0, 1, 0)
```java
if (newState.get(0).equals(1) && newState.get(2).equals(1)) {
    newState.set(0, 0);
    newState.set(1, 1);
    newState.set(2, 0);
    newState.set(3, 1); 
}
if (newState.get(1).equals(1) && newState.get(3).equals(1)) {
    newState.set(0, 1);
    newState.set(1, 0);
    newState.set(2, 1);
    newState.set(3, 0); 
}
```
After these operations, it updates the cell state and visualization.
## Modeling results
![](/image/1.jpg )  
![](/image/2.jpg )  
![](/image/3.jpg )  
## Contact
If you have any comments or concerns, please don't hesitate to aske via my email :)
