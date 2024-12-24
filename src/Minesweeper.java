import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    
    private class MineTile extends JButton {
        int r;
        int c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 30;
    int numRows;
    int numCols;
    int boardWidth;
    int boardHeight;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JLabel timerLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JPanel difficultyPanel = new JPanel();

    int mineCount;
    MineTile[][] board;
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0; // Reveal các ô, trừ ô đánh dấu
    boolean gameOver = false;

    JMenuBar menuBar = new JMenuBar();
    JMenu gameMenu = new JMenu("Game");
    JMenuItem playAgainItem = new JMenuItem("Play Again");
    JMenu difficultyMenu = new JMenu("Difficulty");
    JMenuItem easyItem = new JMenuItem("Easy");
    JMenuItem mediumItem = new JMenuItem("Medium");
    JMenuItem hardItem = new JMenuItem("Hard");

    Timer timer;
    int timeElapsed = 0;

    Minesweeper() {
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setOpaque(true);

        timerLabel.setFont(new Font("Arial", Font.BOLD, 25));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.WEST);
        textPanel.add(timerLabel, BorderLayout.EAST);
        frame.add(textPanel, BorderLayout.NORTH);

        setUpMenu();
        frame.setJMenuBar(menuBar);

        difficultyPanel.setLayout(new GridLayout(1, 3));

        JButton easyButton = new JButton("Easy");
        easyButton.setFont(new Font("Arial", Font.BOLD, 25));
        easyButton.addActionListener(e -> startGame(9, 9, 10));

        JButton mediumButton = new JButton("Medium");
        mediumButton.setFont(new Font("Arial", Font.BOLD, 25));
        mediumButton.addActionListener(e -> startGame(16, 16, 40));

        JButton hardButton = new JButton("Hard");
        hardButton.setFont(new Font("Arial", Font.BOLD, 25));
        hardButton.addActionListener(e -> startGame(16, 30, 100));

        difficultyPanel.add(easyButton);
        difficultyPanel.add(mediumButton);
        difficultyPanel.add(hardButton);

        frame.add(difficultyPanel, BorderLayout.CENTER);

        frame.setSize(600, 150);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }

    void setUpMenu() {
        playAgainItem.addActionListener(e -> playAgain());

        easyItem.addActionListener(e -> startGame(9, 9, 10));
        mediumItem.addActionListener(e -> startGame(16, 16, 40));
        hardItem.addActionListener(e -> startGame(16, 30, 100));

        difficultyMenu.add(easyItem);
        difficultyMenu.add(mediumItem);
        difficultyMenu.add(hardItem);

        gameMenu.add(playAgainItem);
        gameMenu.add(difficultyMenu);

        menuBar.add(gameMenu);
    }

    void startGame(int rows, int cols, int mines) {
        numRows = rows;
        numCols = cols;
        mineCount = mines;
        boardWidth = numCols * tileSize;
        boardHeight = numRows * tileSize;

        // Dừng bộ đếm thời gian hiện tại nếu đang chạy
        stopTimer();

        frame.remove(difficultyPanel);
        frame.setSize(boardWidth + 20, boardHeight + 70);
        frame.setLocationRelativeTo(null);

        gameOver = false;
        tilesClicked = 0;
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(numRows, numCols));
        board = new MineTile[numRows][numCols];

        textLabel.setText("Mines: " + mineCount);
        timerLabel.setText("Time: 0");

        initializeBoard();
        frame.add(boardPanel, BorderLayout.CENTER);
        boardPanel.revalidate();
        boardPanel.repaint();

        setMines();

        // Đặt lại timeElapsed trước khi bắt đầu bộ đếm thời gian mới
        timeElapsed = 0;
        startTimer();
    }


    void playAgain() {
        startGame(numRows, numCols, mineCount);
    }

    void initializeBoard() {
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 15));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        // left and right click together
                        if (e.getModifiersEx() == (MouseEvent.BUTTON1_DOWN_MASK | MouseEvent.BUTTON3_DOWN_MASK)) {
                            revealSurroundingCells(tile.r, tile.c);
                        } else if (e.getButton() == MouseEvent.BUTTON1) {
                            if (tile.getText() == "") {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }
                        // right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (tile.getText() == "" && tile.isEnabled()) {
                                tile.setText("🚩");
                            } else if (tile.getText() == "🚩") {
                                tile.setText("");
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }
    }

    void setMines() {
        mineList = new ArrayList<MineTile>();

        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft -= 1;
            }
        }
    }

    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setText("💣");
        }

        gameOver = true;
        textLabel.setText("Game Over!");
        stopTimer();
    }

    void revealSurroundingCells(int r, int c) {
        int flaggedCount = 0;
        ArrayList<MineTile> surroundingCells = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newRow = r + i;
                int newCol = c + j;
                if (newRow >= 0 && newRow < numRows && newCol >= 0 && newCol < numCols) {
                    MineTile tile = board[newRow][newCol];
                    if (tile.getText().equals("🚩")) {
                        flaggedCount++;
                    }
                    surroundingCells.add(tile);
                }
            }
        }

        MineTile currentTile = board[r][c];
        if (flaggedCount == Integer.parseInt(currentTile.getText())) {
            for (MineTile tile : surroundingCells) {
                if (!tile.getText().equals("🚩") && tile.isEnabled()) {
                    checkMine(tile.r, tile.c);
                }
            }
        }
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled() || tile.getText().equals("🚩")) {
            return;
        }

        tile.setEnabled(false);
        tilesClicked += 1;

        int minesFound = 0;

        if (mineList.contains(tile)) {
            revealMines();
            return;
        }

        // top 3
        minesFound += countMine(r - 1, c - 1);
        minesFound += countMine(r - 1, c);
        minesFound += countMine(r - 1, c + 1);

        // left and right
        minesFound += countMine(r, c - 1); // left
        minesFound += countMine(r, c + 1); // right

        // bottom 3
        minesFound += countMine(r + 1, c - 1); // bottom left
        minesFound += countMine(r + 1, c); // bottom
        minesFound += countMine(r + 1, c + 1); // bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");

            // top 3
            checkMine(r - 1, c - 1); // top left
            checkMine(r - 1, c); // top
            checkMine(r - 1, c + 1); // top right

            // left and right
            checkMine(r, c - 1); // left
            checkMine(r, c + 1); // right

            // bottom 3
            checkMine(r + 1, c - 1); // bottom left
            checkMine(r + 1, c); // bottom
            checkMine(r + 1, c + 1); // bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared!");
            stopTimer();
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        if (mineList.contains(board[r][c])) {
            return 1;
        }
        return 0;
    }

    void startTimer() {
        timeElapsed = 0;
        timer = new Timer(1000, e -> {
            timeElapsed++;
            timerLabel.setText("Time: " + timeElapsed);
        });
        timer.start();
    }

    void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

}