import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

public class MusicPlayerGUI extends JFrame {

    private AudioInputStream song;
    private Clip clip;
    private String path = "C:\\Users\\himan\\Desktop\\Music-Player\\Source-Code\\lib\\";
    private File[] musicFiles;
    private String imagePath = "C:\\Users\\himan\\Desktop\\Working Model\\Music-Player\\Source-Code\\icons\\";
    private int currentSongIndex = 0;
    private long clipTimePosition = 0; // To store the position when paused

    private JButton playPause = new JButton();
    private JButton loop = new JButton();
    private JButton mute = new JButton();
    private JButton next = new JButton();
    private JButton previous = new JButton();

    private JLabel statusLabel = new JLabel("Status: Stopped");

    private JLabel musicListLabel = new JLabel("Music List:");
    private JTextArea musicListTextArea = new JTextArea(10, 30);

    private boolean isPlaying = false;
    private boolean isMuted = false;

    // New components for the progress bar
    private JProgressBar progressBar = new JProgressBar(0, 100);
    private boolean isAdjustingProgress = false;

    public MusicPlayerGUI() throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        //App icon setup ...
        String frameIconPath = imagePath + "music-note.png";
        ImageIcon frameIcon = new ImageIcon(frameIconPath);
        this.setIconImage(frameIcon.getImage());
        ////////////////////

        this.setTitle("V-Player");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(new Color(200, 200, 200));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        buttonPanel.setBackground(new Color(255, 255, 255));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        customizeButton(playPause, "play.png", 100, 100);
        customizeButton(previous, "previous.png",100,100);
        customizeButton(next, "next-button.png",100,100);
        customizeButton(loop, "list.png",100,100);
        customizeButton(mute, "enable-sound.png",100,100);

        buttonPanel.add(previous);
        buttonPanel.add(playPause);
        buttonPanel.add(next);
        buttonPanel.add(loop);
        buttonPanel.add(mute);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(new Color(200, 200, 200));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        customizeLabel(statusLabel);
        customizeLabel(musicListLabel);

        musicListTextArea.setFont(new Font("Arial", Font.PLAIN, 16));
        musicListTextArea.setEditable(false);
        musicListTextArea.setBackground(new Color(230, 230, 230));
        musicListTextArea.setForeground(Color.BLACK);

        statusPanel.add(statusLabel);
        statusPanel.add(musicListLabel);
        statusPanel.add(musicListTextArea);

        // Add progress bar
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Handle seeking when the progress bar is clicked
                if (clip != null && clip.isRunning()) {
                    int mouseX = e.getX();
                    int progressBarWidth = progressBar.getWidth();
                    float percentage = (float) mouseX / progressBarWidth;
                    long newPosition = (long) (clip.getMicrosecondLength() * percentage);
                    clip.setMicrosecondPosition(newPosition);
                    updateProgressBar();
                }
            }
        });

        statusPanel.add(progressBar); // Add progress bar to the status panel

        this.add(buttonPanel, BorderLayout.CENTER);
        this.add(statusPanel, BorderLayout.SOUTH);
        this.pack();
        this.setMinimumSize(new Dimension(500, 300));
        this.setMaximumSize(new Dimension(800, 500));
        this.setVisible(true);

        musicFiles = new File(path).listFiles((dir, name) -> name.toLowerCase().endsWith(".wav"));

        if (musicFiles != null && musicFiles.length > 0) {
            updateMusicList();

            playPause.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (isPlaying) {
                            pauseMusic();
                        } else {
                            if (clipTimePosition > 0) {
                                clip.setMicrosecondPosition(clipTimePosition);
                                clip.start();
                            } else {
                                if (clip != null && clip.isOpen()) {
                                    clip.start();
                                } else {
                                    playMusic(musicFiles[currentSongIndex]);
                                }
                            }
                        }
                        isPlaying = !isPlaying;
                        updateStatusLabel();
                    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            loop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Loop button clicked");
                }
            });

            mute.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        toggleMute();
                    } catch (LineUnavailableException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            next.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        nextSong();
                    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            previous.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        previousSong();
                    } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e1) {
                        e1.printStackTrace();
                    }
                }
            });

        } else {
            statusLabel.setText("No .wav files found in the directory.");
        }
    }

    private void playMusic(File musicFile) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        song = AudioSystem.getAudioInputStream(musicFile);
        clip = AudioSystem.getClip();

        clip.open(song);
        clip.start();

        clip.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if (event.getType() == LineEvent.Type.STOP) {
                    if (isPlaying) {
                        try {
                            nextSong();
                        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
                            e.printStackTrace();
                        }
                    }
                    updateProgressBar();
                }
            }
        });

        // Add this line to update the progress bar continuously
        Timer timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProgressBar();
            }
        });
        timer.start();

        updateStatusLabel();
    }

    private void pauseMusic() {
        if (clip != null && clip.isRunning()) {
            clipTimePosition = clip.getMicrosecondPosition();
            clip.stop();
            updateStatusLabel();
        }
    }

    private void stopMusic() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clipTimePosition = 0;
            updateStatusLabel();
        }
    }

    private void muteMusic() {
        if (clip != null) {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(volumeControl.getMinimum());
                updateStatusLabel();
            }
        }
    }

    private void toggleMute() throws LineUnavailableException {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (isMuted) {
                volumeControl.setValue(0.0f);
                mute.setText("Mute");
            } else {
                volumeControl.setValue(volumeControl.getMinimum());
                mute.setText("Unmute");
            }
            isMuted = !isMuted;
            updateStatusLabel();
        }
    }

    private void nextSong() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        stopMusic();
        currentSongIndex = (currentSongIndex + 1) % musicFiles.length;
        playMusic(musicFiles[currentSongIndex]);
        progressBar.setValue(0); // Reset progress bar
    }

    private void previousSong() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        stopMusic();
        currentSongIndex = (currentSongIndex - 1 + musicFiles.length) % musicFiles.length;
        playMusic(musicFiles[currentSongIndex]);
        progressBar.setValue(0); // Reset progress bar
    }

    private void updateStatusLabel() {
        String statusText = "Status: ";
        if (isPlaying) {
            statusText += "Now Playing - " + musicFiles[currentSongIndex].getName();
            playPause.setIcon(new ImageIcon(imagePath + (clip.isRunning() ? "pause.png" : "play.png")));
        } else {
            statusText += "Stopped";
            playPause.setIcon(new ImageIcon(imagePath + "play.png"));
        }
        if (isMuted) {
            statusText += " (Muted)";
        }
        statusLabel.setText(statusText);
    }

    private void updateMusicList() {
        StringBuilder musicList = new StringBuilder();
        for (File file : musicFiles) {
            musicList.append(file.getName()).append("\n");
        }
        musicListTextArea.setText(musicList.toString());
    }

    private void customizeButton(JButton button, String imageName, int width, int height) {
        ImageIcon originalIcon = new ImageIcon(imagePath + imageName);
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        button.setIcon(scaledIcon);
        button.setPressedIcon(scaledIcon);
        button.setBackground(new Color(255, 255, 255));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
    }
    

    private void customizeLabel(JLabel label) {
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        label.setForeground(Color.BLACK);
    }

    private void updateProgressBar() {
        if (clip != null && clip.isOpen() && !isAdjustingProgress) {
            long position = clip.getMicrosecondPosition();
            long length = clip.getMicrosecondLength();
            int progressValue = (int) ((position * 100) / length);
            progressBar.setValue(progressValue);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new MusicPlayerGUI();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        });
    }
}
