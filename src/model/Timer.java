package model;


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
    
    
    public Timer(long initialTimeMs, long incrementMs) {
        this.whiteTimeMs = initialTimeMs;
        this.blackTimeMs = initialTimeMs;
        this.whiteIncrementMs = incrementMs;
        this.blackIncrementMs = incrementMs;
        this.timerRunning = false;
        this.activePlayer = Color.WHITE;
    }
    
    
    public Timer(long initialTimeMs, long whiteIncrementMs, long blackIncrementMs) {
        this.whiteTimeMs = initialTimeMs;
        this.blackTimeMs = initialTimeMs;
        this.whiteIncrementMs = whiteIncrementMs;
        this.blackIncrementMs = blackIncrementMs;
        this.timerRunning = false;
        this.activePlayer = Color.WHITE;
    }
    
    
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
    
    
    public synchronized void switchPlayer() {
        activePlayer = activePlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
    }
    
    
    public synchronized long getRemainingTimeMs(Color color) {
        return color == Color.WHITE ? whiteTimeMs : blackTimeMs;
    }
    
    
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
    
    
    public synchronized boolean isTimeOut(Color color) {
        return getRemainingTimeMs(color) <= 0;
    }
    
    
    public synchronized void reset(long initialTimeMs) {
        this.whiteTimeMs = initialTimeMs;
        this.blackTimeMs = initialTimeMs;
        this.timerRunning = false;
        stopTicking = true;
    }
    
    
    public synchronized void pause() {
        timerRunning = false;
    }
    
    
    public synchronized void resume(Color sideToMove) {
        startTimer(sideToMove);
    }
    
    
    public synchronized long getTotalTimeMs() {
        return whiteTimeMs + blackTimeMs;
    }
    
    
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
