package org.infpls.noxio.game.module.game.game;

public class Score {
  private static final int MULTI_TIMER_LENGTH = 150; // Timer for multikills
  
  /* Number of credits awareded for various actions */
  private static final int 
    KILL_C = 10,
    WIN_C = 50,
    NEUTRAL_C = 25,
    LOSE_C = 10,
    BETRAYL_C = -5,
    BETRAYED_C = 5,
    FIRST_C = 10,
    KILLJOY_C = 10,
    ENDREIGN_C = 25,
    FLAGCAP_C = 50,
    FLAGDEF_C = 10,
    BOMB_C = 50,
    BOMBDEF_C = 25,
    HILL_C = 5,
    ULT_C = 10,
    RAB_C = 15,
    TAG_C = 10,
    PERFECT_C = 100,
    MULTIX_C = 10,
    SPREEX_C = 5;
          
  private int kills, deaths, objectives, spree;
  private int multi, timer;
  
  private final Stats stats;
  public Score() {
    kills = 0;
    deaths = 0;
    objectives = 0;
    spree = 0;
    
    multi = 0; timer = 0;
    
    stats = new Stats();
  }
    
  public void kill(int frame) {
    kills++; spree++;
    stats.kill++;
    stats.credits += KILL_C;
    if(frame - timer <= MULTI_TIMER_LENGTH) { multi++; }
    else { multi = 1; }
    timer = frame;
  }
  public int death() {
    deaths++;
    stats.death++;
    int s = spree;
    spree = 0;
    return s;
  }
  
  public void win() { stats.gameWin++; stats.credits += WIN_C; }
  public void neutral() { stats.credits += NEUTRAL_C; }
  public void lose() { if(deaths>0) { stats.gameLose++; } stats.credits += LOSE_C; } /* Don't count a loss unless you died. Prevents joining an active game and losing immiedately. */
  
  public void betrayl() { stats.betrayl++; stats.credits += BETRAYL_C; }
  public void betrayed() { stats.betrayed++; stats.credits += BETRAYED_C; }
  
  public void firstBlood() { stats.firstBlood++; stats.credits += FIRST_C; }
  public void killJoy() { stats.killJoy++; stats.credits += KILLJOY_C; }
  public void endedReign() { stats.endedReign++; stats.credits += ENDREIGN_C; }
  
  public void flagCapture() { objectives++; stats.flagCapture++; stats.credits += FLAGCAP_C; }
  public void flagDefense() { stats.flagDefense++; stats.credits += FLAGDEF_C; }
  public void bomb() { objectives++; stats.credits += BOMB_C; }
  public void bombDefense() { stats.credits += BOMBDEF_C; }
  public void hillControl() { objectives++; stats.hillControl++; stats.credits += HILL_C; }
  public void ultimateControl() { objectives++; stats.credits += ULT_C; }
  public void rabbitControl() { objectives++; stats.credits += RAB_C; }
  public void tagControl() { objectives++; stats.credits += TAG_C; }
  
  public void perfect() { stats.perfect++; stats.credits += PERFECT_C; }
  public void humiliation() { stats.humiliation++; }
  
  public int getMulti() {
    switch(multi) {
      case 2 : { stats.mkx02++; stats.credits += (MULTIX_C*multi); break; }
      case 3 : { stats.mkx03++; stats.credits += (MULTIX_C*multi); break; }
      case 4 : { stats.mkx04++; stats.credits += (MULTIX_C*multi); break; }
      case 5 : { stats.mkx05++; stats.credits += (MULTIX_C*multi); break; }
      case 6 : { stats.mkx06++; stats.credits += (MULTIX_C*multi); break; }
      case 7 : { stats.mkx07++; stats.credits += (MULTIX_C*multi); break; }
      case 8 : { stats.mkx08++; stats.credits += (MULTIX_C*multi); break; }
      case 9 : { stats.mkx09++; stats.credits += (MULTIX_C*multi); break; }
      case 10 : { stats.mkx10++; stats.credits += (MULTIX_C*multi); break; }
      case 11 : { stats.mkx11++; stats.credits += (MULTIX_C*multi); break; }
      case 12 : { stats.mkx12++; stats.credits += (MULTIX_C*multi); break; }
      case 13 : { stats.mkx13++; stats.credits += (MULTIX_C*multi); break; }
      case 14 : { stats.mkx14++; stats.credits += (MULTIX_C*multi); break; }
      case 15 : { stats.mkx15++; stats.credits += (MULTIX_C*multi); break; }
      case 16 : { stats.mkx16++; stats.credits += (MULTIX_C*multi); break; }
      case 17 : { stats.mkx17++; stats.credits += (MULTIX_C*multi); break; }
      case 18 : { stats.mkx18++; stats.credits += (MULTIX_C*multi); break; }
      case 19 : { stats.mkx19++; stats.credits += (MULTIX_C*multi); break; }
      case 20 : { stats.mkx20++; stats.credits += (MULTIX_C*multi); break; }
    }
    return multi;
  }
  public int getSpree() {
    switch(spree) {
      case 5 : { stats.ksx05++; stats.credits += (SPREEX_C*spree); break; }
      case 10 : { stats.ksx10++; stats.credits += (SPREEX_C*spree); break; }
      case 15 : { stats.ksx15++; stats.credits += (SPREEX_C*spree); break; }
      case 20 : { stats.ksx20++; stats.credits += (SPREEX_C*spree); break; }
      case 25 : { stats.ksx25++; stats.credits += (SPREEX_C*spree); break; }
      case 30 : { stats.ksx30++; stats.credits += (SPREEX_C*spree); break; }
    }
    return spree;
  }
  
  public int getObjectives() { return objectives; }
  public int getKills() { return kills; }
  public int getDeaths() { return deaths; }
  
  private int creditChange = 0;
  public int getCreditChange() { final int ch = stats.credits - creditChange; creditChange = stats.credits; return ch; }
  
  public Stats getStats() { return stats; }
  
  public class Stats {
    public int credits;
    public int kill, death, gameWin, gameLose, betrayed, betrayl;
    public int firstBlood, killJoy, endedReign, flagCapture, flagDefense, hillControl;
    public int perfect, humiliation;
    public int mkx02, mkx03, mkx04, mkx05, mkx06, mkx07, mkx08, mkx09, mkx10, mkx11, mkx12, mkx13, mkx14, mkx15, mkx16, mkx17, mkx18, mkx19, mkx20;
    public int ksx05, ksx10, ksx15, ksx20, ksx25, ksx30;
    public int cumRes;
    
    public Stats() {
      credits = 0;
      kill = 0; death = 0; gameWin = 0; gameLose = 0; betrayed = 0; betrayl = 0;
      firstBlood = 0; killJoy = 0; endedReign = 0; flagCapture = 0; flagDefense = 0; hillControl = 0;
      perfect = 0; humiliation = 0;
      mkx02 = 0; mkx03 = 0; mkx04 = 0; mkx05 = 0; mkx06 = 0; mkx07 = 0; mkx08 = 0; mkx09 = 0; mkx10 = 0;
      mkx11 = 0; mkx12 = 0; mkx13 = 0; mkx14 = 0; mkx15 = 0; mkx16 = 0; mkx17 = 0; mkx18 = 0; mkx19 = 0; mkx20 = 0;
      ksx05 = 0; ksx10 = 0; ksx15 = 0; ksx20 = 0; ksx25 = 0; ksx30 = 0;
      cumRes = 0;
    }
  }
}
