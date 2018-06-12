package org.infpls.noxio.game.module.game.game;

public class Score {
  private static enum SSFX { /* Score Sound Effect */
    none(-1), minor(0), kill(1), big(2), major(3), global(4);
    
    public final int id;
    SSFX(int id) {
      this.id = id;
    }
  }
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
    KILLOBJ_C = 25,
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
    addCredits(KILL_C, SSFX.kill);
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
  
  public void win() { stats.gameWin++; addCredits(WIN_C, SSFX.global); }
  public void neutral() { addCredits(NEUTRAL_C, SSFX.global); }
  public void lose() { if(deaths>0) { stats.gameLose++; } addCredits(LOSE_C, SSFX.global); } /* Don't count a loss unless you died. Prevents joining an active game and losing immiedately. */
  
  public void betrayl() { stats.betrayl++; addCredits(BETRAYL_C, SSFX.minor); }
  public void betrayed() { stats.betrayed++; addCredits(BETRAYED_C, SSFX.minor); }
  
  public void firstBlood() { stats.firstBlood++; addCredits(FIRST_C, SSFX.kill); }
  public void killJoy() { stats.killJoy++; addCredits(KILLJOY_C, SSFX.big); }
  public void endedReign() { stats.endedReign++; addCredits(ENDREIGN_C, SSFX.big); }
  
  public void flagCapture() { objectives++; stats.flagCapture++; addCredits(FLAGCAP_C, SSFX.global); }
  public void flagDefense() { stats.flagDefense++; addCredits(FLAGDEF_C, SSFX.major); }
  public void bomb() { objectives++; addCredits(BOMB_C, SSFX.global); }
  public void bombDefense() { addCredits(BOMBDEF_C, SSFX.major); }
  public void hillControl() { objectives++; stats.hillControl++; addCredits(HILL_C, SSFX.minor); }
  public void ultimateControl() { objectives++; addCredits(ULT_C, SSFX.minor); }
  public void rabbitControl() { objectives++; addCredits(RAB_C, SSFX.minor); }
  public void tagControl() { objectives++; addCredits(TAG_C, SSFX.minor); }
  public void killObjective() { objectives++; addCredits(KILLOBJ_C, SSFX.major); }
  
  public void perfect() { stats.perfect++; addCredits(PERFECT_C, SSFX.global); }
  public void humiliation() { stats.humiliation++; }
  
  public int getMulti() {
    switch(multi) {
      case 2 : { stats.mkx02++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 3 : { stats.mkx03++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 4 : { stats.mkx04++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 5 : { stats.mkx05++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 6 : { stats.mkx06++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 7 : { stats.mkx07++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 8 : { stats.mkx08++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 9 : { stats.mkx09++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 10 : { stats.mkx10++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 11 : { stats.mkx11++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 12 : { stats.mkx12++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 13 : { stats.mkx13++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 14 : { stats.mkx14++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 15 : { stats.mkx15++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 16 : { stats.mkx16++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 17 : { stats.mkx17++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 18 : { stats.mkx18++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 19 : { stats.mkx19++; addCredits((MULTIX_C*multi), SSFX.big); break; }
      case 20 : { stats.mkx20++; addCredits((MULTIX_C*multi), SSFX.big); break; }
    }
    return multi;
  }
  public int getSpree() {
    switch(spree) {
      case 5 : { stats.ksx05++; addCredits((SPREEX_C*spree), SSFX.big); break; }
      case 10 : { stats.ksx10++; addCredits((SPREEX_C*spree), SSFX.big); break; }
      case 15 : { stats.ksx15++; addCredits((SPREEX_C*spree), SSFX.big); break; }
      case 20 : { stats.ksx20++; addCredits((SPREEX_C*spree), SSFX.big); break; }
      case 25 : { stats.ksx25++; addCredits((SPREEX_C*spree), SSFX.big); break; }
      case 30 : { stats.ksx30++; addCredits((SPREEX_C*spree), SSFX.big); break; }
    }
    return spree;
  }
  
  public int getObjectives() { return objectives; }
  public int getKills() { return kills; }
  public int getDeaths() { return deaths; }
  
  private int creditChange = 0, ssfxId = -1; // 0=accumulation, 1=kill, 2=big-kill, 3=objective, 4=global-objective(silent)
  public int[] getCreditChange() { final int ch = stats.credits - creditChange; creditChange = stats.credits; int cpy = ssfxId; ssfxId = -1; return new int[]{ ch, cpy }; }
  private void addCredits(int amount, SSFX ssfx) {
    stats.credits += amount;
    ssfxId = ssfxId < ssfx.id ? ssfx.id : ssfxId;
  }
  
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
