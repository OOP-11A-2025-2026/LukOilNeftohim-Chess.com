package model;

/**
 * Timer for chess game with real-time ticking
 * Tracks remaining time for each player with background thread
 */
public class Timer {
    private long whiteTimeMs;
    private long blackTimeMs;
    private long whiteIncrementMs;
    private long blackIncrementMs;
    
    private Color activePlayer;
    private long lastTickTime;
    private boolean timerRunning;
    private Thread tickThread;
    private volatile boolean stopTicking = false;
    
    /**
     * Create a timer with initial time and optional increment
     * @param initialTimeMs initial time in milliseconds for each player
     * @param incrementMs increment in milliseconds per move
     */
    public Timer(long initialTimeMs, long incrementMs) {
        this.whiteTimeMs = initialTimeMs;
        this.blackTimeMs = initialTimeMs;
        this.whiteIncrementMs = incrementMs;
        this.blackIncrementMs = incrementMs;
        this.timerRunning = false;
        this.activePlayer = Color.WHITE;
    }
    
    /**
     * Create a timer with different increments for each side
     */
    public Timer(long initialTimeMs, long whiteIncrementMs, long blackIncrementMs) {
        this.whiteTimeMs = initialTimeMs;
        this.blackTimeMs = initialTimeMs;
        this.whiteIncrementMs = whiteIncrementMs;
        this.blackIncrementMs = blackIncrementMs;
        this.timerRunning = false;
        this.activePlayer = Color.WHITE;
    }
    
    /**
     * Start the timer for a player's turn
     * Starts a background thread that decrements time in real-time
     */
    public synchronized void startTimer(Color sideToMove) {
        this.activePlayer = sideToMove;
        this.lastTickTime = System.currentTimeMillis();
        this.timerRunning = true;
        
        // Start the ticking thread if not already running
        if (tickThread == null || !tickThread.isAlive()) {
            stopTicking = false;
            tickThread = new Thread(this::tickClock);
            tickThread.setDaemon(true);
            tickThread.start();
        }
    }
    
    /**
     * Background thread that decrements time in real-time
     */
    private void tickClock() {
        while (!stopTicking) {
            if (timerRunning) {
                long currentTime = System.currentTimeMillis();
                long elapsed = currentTime - lastTickTime;
                
                if (elapsed >= 50) { // Update every 50ms
                    synchronized (this) {
                        if (activePlayer == Color.WHITE) {
                            whiteTimeMs -= elapsed;
                            if (whiteTimeMs < 0) whiteTimeMs = 0;
                        } else {
                            blackTimeMs -= elapsed;
                            if (blackTimeMs < 0) blackTimeMs = 0;
                        }
                    }
                    lastTickTime = currentTime;
                }
            }
            
            try {
                Thread.sleep(20); // Check every 20ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Stop the timer and switch to the other player
     * Should be called after each move
     */
    public synchronized void stopTimer() {
        if (!timerRunning) return;
        
        // Add increment to the player who just moved
        if (activePlayer == Color.WHITE) {
            whiteTimeMs += whiteIncrementMs;
        } else {
            blackTimeMs += blackIncrementMs;
        }
        
        // Ensure time doesn't go below 0
        if (whiteTimeMs < 0) whiteTimeMs = 0;
        if (blackTimeMs < 0) blackTimeMs = 0;
        
        timerRunning = false;
    }
    
    /**
     * Switch to the other player's turn
     */
    public synchronized void switchPlayer() {
        activePlayer = activePlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
    
    /**
     * Get remaining time for a player in milliseconds
     */
    public synchronized long getRemainingTimeMs(Color color) {
        return color == Color.WHITE ? whiteTimeMs : blackTimeMs;
    }
    
    /**
     * Get remaining time formatted as MM:SS or MM:SS.D
     */
    public synchronized String getFormattedTime(Color color) {
        long timeMs = getRemainingTimeMs(color);
        if (timeMs < 0) timeMs = 0;
        
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        long secs = seconds % 60;
        long deciseconds = (timeMs % 1000) / 100;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, secs);
        } else {
            return String.format("%d.%d", secs, deciseconds);
        }
    }
    
    /**
     * Check if a player has run out of time
     */
    public synchronized boolean isTimeOut(Color color) {
        return getRemainingTimeMs(color) <= 0;
    }
    
    /**
     * Reset the timer
     */
    public synchronized void reset(long initialTimeMs) {
        this.whiteTimeMs = initialTimeMs;
        this.blackTimeMs = initialTimeMs;
        this.timerRunning = false;
        stopTicking = true;
    }
    
    /**
     * Pause the timer
     */
    public synchronized void pause() {
        timerRunning = false;
    }
    
    /**
     * Resume the timer
     */
    public synchronized void resume(Color sideToMove) {
        startTimer(sideToMove);
    }
    
    /**
     * Get total time remaining for both players
     */
    public synchronized long getTotalTimeMs() {
        return whiteTimeMs + blackTimeMs;
    }
    
    /**
     * Stop the timer thread cleanly
     */
    public synchronized void shutdown() {
        stopTicking = true;
        timerRunning = false;
        if (tickThread != null) {
            try {
                tickThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
