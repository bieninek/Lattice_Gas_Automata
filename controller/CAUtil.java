package controller;

import model.Cell;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;


public class CAUtil {
    private ArrayList<ArrayList<Cell>> cells;
    BufferedImage image;

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

    // generating particles - the red ones
    // values in loops can be modified to achieve desired locations for gas
    private void generateParticles() throws IOException {
        for (int i = 13; i < 128; i++) {
            for (int j = 13; j < 388; j++) {
                Random rnd = new Random();
                int probability = 100;
                if (rnd.nextInt() < probability)
                    this.image.setRGB(i, j, Color.red.getRGB());
            }
        }

        (new FileUtil()).saveToFile(this.image);
    }

    // initialization, one-time invoked
    // basing on created image, cells are classified as borders or not
    // also, initial directions are tossed pseudo randomly with the same chance for the all directions
    private void guessType() {
        // iteration through all the pixels on the input image
        for (int i = 0; i < this.image.getHeight(); i++) {
            for (int j = 0; j < this.image.getWidth(); j++) {

                // type guessing
                if (new Color(this.image.getRGB(j, i)).equals(Color.black)) { // border
                    cells.get(i).add(new Cell(Cell.cellType.BORDER));
                } else if (new Color(this.image.getRGB(j, i)).equals(Color.white)) { // no border nor particles
                    cells.get(i).add(new Cell(Cell.cellType.NORMAL));
                } else if (new Color(this.image.getRGB(j, i)).equals(Color.red)) { // particle exists
                    Cell cell = new Cell(Cell.cellType.NORMAL);

                    // particle exists, so I need to set initial direction
                    Random rand = new Random();
                    int tmp = rand.nextInt(4) + 1;
                    ArrayList<Integer> newState = switch (tmp) {
                        case 1 -> new ArrayList<>(Arrays.asList(1, 0, 0, 0));
                        case 2 -> new ArrayList<>(Arrays.asList(0, 1, 0, 0));
                        case 3 -> new ArrayList<>(Arrays.asList(0, 0, 1, 0));
                        case 4 -> new ArrayList<>(Arrays.asList(0, 0, 0, 1));
                        default -> new ArrayList<>(Arrays.asList(0, 0, 0, 0));
                    };

                    // adding new cell in row
                    cell.setState(newState);
                    cells.get(i).add(cell);
                } else {
                    System.out.print(new Color(this.image.getRGB(j, i)).getRed() + "\t" +
                            new Color(this.image.getRGB(j, i)).getGreen() + "\t" +
                            new Color(this.image.getRGB(j, i)).getBlue() + "\t");
                    System.out.println("initial state can not be set");
                    System.out.println("\t" + i + "\t" + j);
                    cells.get(i).add(new Cell(Cell.cellType.BORDER));
                }
            }
        }
    }

    // calculates solution for each step of simulation
    public BufferedImage simulate() throws IOException {
        BufferedImage img = (new FileUtil()).openFromFile();

        // prepare data structure for new states to avoid overwriting
        ArrayList<ArrayList<Cell>> newCells = new ArrayList<>(img.getHeight());
        for (int i = 0; i < img.getHeight(); i++) {
            newCells.add(new ArrayList<>(img.getWidth()));
        }

        // detects border in the first column
        for (int i = 0; i < img.getHeight(); i++) {
            Cell newCell = new Cell(Cell.cellType.BORDER);
            ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
            newCell.setState(newState);
            newCells.get(i).add(newCell);
            img.setRGB(0, i, Color.black.getRGB());
        }

        // detects border in the first row
        for (int i = 0; i < img.getWidth(); i++) {
            Cell newCell = new Cell(Cell.cellType.BORDER);
            ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
            newCell.setState(newState);
            newCells.get(0).add(newCell);
            img.setRGB(i, 0, Color.black.getRGB());
        }

        // iterates through the main part of the image
        for (int i = 1; i < img.getHeight()-1; i++) {
            for (int j = 1; j < img.getWidth()-1; j++) {

                // detects border, if detected goes to another iteration
                if (cells.get(i).get(j).getType() == Cell.cellType.BORDER) {
                    Cell newCell = new Cell(Cell.cellType.BORDER);
                    ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
                    newCell.setState(newState);
                    newCells.get(i).add(newCell);

                // no border
                } else if (cells.get(i).get(j).getType() == Cell.cellType.NORMAL) {
                    // preparing data structures for further analyze
                    Cell newCell = new Cell(Cell.cellType.NORMAL);
                    ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
                    ArrayList<Integer> state = this.cells.get(i).get(j).getState();
                    Cell cellUp = this.cells.get(i-1).get(j);
                    Cell cellDown = this.cells.get(i+1).get(j);
                    Cell cellRight = this.cells.get(i).get(j+1);
                    Cell cellLeft = this.cells.get(i).get(j-1);

                    // checks if reflects from border
                    if (state.get(0).equals(1) && cellUp.getType().equals(Cell.cellType.BORDER)) { // upper cell is border
                        newState.set(2, 1);
                    } else if (state.get(1).equals(1) && cellRight.getType().equals(Cell.cellType.BORDER)) { // right cell is border
                        newState.set(3, 1);
                    } else if (state.get(2).equals(1) && cellDown.getType().equals(Cell.cellType.BORDER)) { // bottom cell is border
                        newState.set(0, 1);
                    } else if (state.get(3).equals(1) && cellLeft.getType().equals(Cell.cellType.BORDER)) { // left cell is border
                        newState.set(1, 1);

                    } else {
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

                        // collision
                        if (newState.get(0).equals(1) && newState.get(2).equals(1)) {
                            newState.set(0, 0);
                            newState.set(1, 1);
                            newState.set(2, 0);
                            newState.set(3, 1);
                        } else if (newState.get(1).equals(1) && newState.get(3).equals(1)) {
                            newState.set(0, 1);
                            newState.set(1, 0);
                            newState.set(2, 1);
                            newState.set(3, 0);
                        }
                    }
                    newCell.setState(newState);
                    newCells.get(i).add(newCell);

                    // board coloring
                    if (newCells.get(i).get(j).getType() == Cell.cellType.BORDER) { // border
                        img.setRGB(j, i, Color.black.getRGB());
                    }
                    if (newState.get(0).equals(1) || newState.get(1).equals(1) ||
                            newState.get(2).equals(1) || newState.get(3).equals(1)) { // particle exists in this cell
                        img.setRGB(j, i, Color.red.getRGB());
                    } else { // no particles in this cell
                        img.setRGB(j, i, Color.white.getRGB());
                    }

                } else {
                    System.out.println("error - weird cell type");
                }
            }
        }

        // detects border in the last column
        for (int i = 0; i < img.getHeight(); i++) {
            Cell newCell = new Cell(Cell.cellType.BORDER);
            ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
            newCell.setState(newState);
            newCells.get(i).add(newCell);
            img.setRGB(img.getWidth()-1, i, Color.black.getRGB());
        }

        // detects border in the last row
        for (int i = 0; i < img.getWidth(); i++) {
            Cell newCell = new Cell(Cell.cellType.BORDER);
            ArrayList<Integer> newState = new ArrayList<>(Collections.nCopies(4, 0));
            newCell.setState(newState);
            newCells.get(img.getHeight()-1).add(newCell);
            img.setRGB(i, img.getHeight()-1, Color.black.getRGB());
        }

        this.cells = newCells;
        (new FileUtil()).saveToFile(img);
        return img;
    }
}
