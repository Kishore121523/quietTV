package local.kishore.quiettv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.PathInterpolator;
import android.widget.*;
import java.util.*;

public class HomeActivity extends Activity {
  final ArrayList<Entry> apps = new ArrayList<>();
  static final PathInterpolator FOCUS_EASE=new PathInterpolator(.22f,.61f,.36f,1f);
  final Map<String,Bitmap> launchArtwork=new HashMap<>();
  Bitmap wallpaperCache;
  Wallpaper wallpaper;
  final int[] wallpaperResources={R.drawable.wall_00,R.drawable.wall_01,R.drawable.wall_02,R.drawable.wall_03,R.drawable.wall_04,R.drawable.wall_05,R.drawable.wall_06,R.drawable.wall_07,R.drawable.wall_08,R.drawable.wall_09,R.drawable.wall_11,R.drawable.wall_12,R.drawable.wall_13,R.drawable.wall_15,R.drawable.wall_16,R.drawable.wall_17,R.drawable.wall_18,R.drawable.wall_19,R.drawable.wall_20,R.drawable.wall_21,R.drawable.wall_22,R.drawable.wall_23};
  final String[] wallpaperNames={"Spider-Verse skyline","Midnight valley","Deadpool and Wolverine","Alpine peaks","Spider-Man neon city","Gate to serenity","Spider-Man blue shadows","Sunset lake","Batman in the rain","Purple horizon","Moonlit mountains","Blossoms and moon","Firewatch sunset","Batman silhouette","Misty mountain layers","Synthwave sunset","Crimson warrior","Retro waves","Rick and Morty","Batman emblem","Solar system","Monochrome waves"};

  int rotationIndex;
  final java.util.concurrent.ExecutorService wallpaperWorker=java.util.concurrent.Executors.newSingleThreadExecutor();
  int wallpaperPalette=-1;
  final Map<String,Drawable> artwork = new HashMap<>();
  android.content.SharedPreferences prefs;
  LinearLayout content;
  LinearLayout homePage;
  FrameLayout homeRoot;
  View launchOverlay;
  View launchSource;
  boolean launching;
  android.animation.ValueAnimator launchAnimation;
  boolean returningHome;
  FrameLayout rowViewport;
  int rowStride;
  int activeRow=-1;
  View first;
  String focused = "";
  boolean active;
  int width;

  final Handler handler = new Handler();
  final String[] priorities={"com.apple.atve.amazon.appletv","com.amazon.firetv.youtube","com.netflix.ninja","com.spotify.tv.android","com.amazon.firebat","in.startv.hotstar","com.jio.media.stb.ondemand","com.suntv.sunnxt","com.zee5.amazon","com.mxtech.videoplayer.television","com.surfshark.vpnclient.android","com.amazon.cloud9","com.amazon.bueller.music"};
  static class Entry {String pkg,label; Intent intent; Drawable art; Entry(String p,String l,Intent i,Drawable a){pkg=p;label=l;intent=i;art=a;}}
  int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
  TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setFontFeatureSettings("kern");return t;}
  GradientDrawable rounded(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("home",0);if(!prefs.getBoolean("wallpaper_v2",false))prefs.edit().putInt("palette",3).putBoolean("wallpaper_v2",true).apply();if(!prefs.getBoolean("music_v3",false)){Set<String> hidden=new HashSet<>(prefs.getStringSet("hidden",Collections.emptySet()));hidden.add("com.amazon.cloud9");hidden.add("com.amazon.bueller.music");hidden.remove("com.spotify.tv.android");prefs.edit().putStringSet("hidden",hidden).putBoolean("music_v3",true).apply();}if(!prefs.getBoolean("rotation_v13",false))prefs.edit().putInt("palette",4).putBoolean("rotation_v13",true).apply();if(!prefs.getBoolean("wallpaper_removal_v21",false)){int old=prefs.getInt("palette",4);int updated=old;if(old==20||old==24)updated=4;else if(old>24)updated=old-2;else if(old>20)updated=old-1;prefs.edit().putInt("palette",updated).putBoolean("wallpaper_removal_v21",true).apply();}overridePendingTransition(0,0);getWindow().getDecorView().setSystemUiVisibility(5894);discover();build();}
  @Override protected void onResume(){super.onResume();overridePendingTransition(0,0);active=true;clearLaunch();if(wallpaper!=null)wallpaper.startRotation();getWindow().getDecorView().setSystemUiVisibility(5894);
    if(returningHome&&homePage!=null&&prefs.getBoolean("motion",true)){
      homePage.animate().cancel();homePage.setScaleX(.99f);homePage.setScaleY(.99f);homePage.setAlpha(1f);
      homePage.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(160).setInterpolator(FOCUS_EASE).start();
    }
    returningHome=false;
  }
  @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);overridePendingTransition(0,0);}
  @Override protected void onPause(){active=false;if(wallpaper!=null)wallpaper.stopRotation();if(homePage!=null){homePage.animate().cancel();homePage.setAlpha(1);homePage.setScaleX(1);homePage.setScaleY(1);}super.onPause();}
  @Override protected void onStop(){returningHome=true;clearLaunch();super.onStop();}
  @Override protected void onDestroy(){wallpaperWorker.shutdownNow();super.onDestroy();}
  @Override public void onBackPressed(){if(launching){clearLaunch();return;}if(first!=null)first.requestFocus();showRow(0);}
  @Override public boolean onKeyDown(int key,KeyEvent e){if(launching){if(key==KeyEvent.KEYCODE_BACK)clearLaunch();return true;}if(key==KeyEvent.KEYCODE_MENU){if(e.getRepeatCount()==0)options();return true;}return super.onKeyDown(key,e);}
  void discover(){
    launchArtwork.clear();apps.clear();PackageManager pm=getPackageManager();Map<String,ResolveInfo> map=new HashMap<>();
    for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){
      Intent query=new Intent(Intent.ACTION_MAIN).addCategory(cat);
      for(ResolveInfo r:pm.queryIntentActivities(query,0))if(!map.containsKey(r.activityInfo.packageName))map.put(r.activityInfo.packageName,r);
    }
    HashSet<String> excluded=new HashSet<>(Arrays.asList(getPackageName(),"io.github.toolicious.homeonfire","com.amazon.tv.launcher","com.amazon.tv.settings.v2","com.amazon.tv.parentalcontrols","com.amazon.tv.legal.notices","com.amazon.tv.csapp","com.amazon.tv.livetv","com.amazon.shoptv.client","com.amazon.shoptv.firetv.client","com.amazon.venezia","com.amazon.minitv.android.app","com.amazon.gamehub"));
    for(Map.Entry<String,ResolveInfo> item:map.entrySet()){
      String p=item.getKey();ResolveInfo r=item.getValue();if(excluded.contains(p))continue;
      boolean known=Arrays.asList(priorities).contains(p);boolean user=(r.activityInfo.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)==0;
      if(!known&&!user)continue;
      Intent i=new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(p,r.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
      Drawable art=artwork.get(p);
      if(art==null){try{art=r.activityInfo.loadBanner(pm);if(art==null)art=r.activityInfo.applicationInfo.loadBanner(pm);if(art==null)art=r.loadIcon(pm);}catch(Exception ignored){}if(art!=null)artwork.put(p,art);}
      String label=r.loadLabel(pm).toString();if(p.equals("com.amazon.firebat"))label="Prime Video";if(p.equals("com.amazon.cloud9"))label="Silk Browser";
      apps.add(new Entry(p,label,i,art));
    }
    Collections.sort(apps,(a,b)->{int x=Arrays.asList(priorities).indexOf(a.pkg),y=Arrays.asList(priorities).indexOf(b.pkg);x=x<0?999:x;y=y<0?999:y;return x==y?a.label.compareToIgnoreCase(b.label):x-y;});
  }
  void build(){
    launchArtwork.clear();
    width=(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density);
    FrameLayout root=new FrameLayout(this);homeRoot=root;wallpaper=new Wallpaper(this);root.addView(wallpaper,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout page=new LinearLayout(this);homePage=page;page.setOrientation(1);page.setPadding(dp(38),dp(22),dp(38),dp(12));root.addView(page,new FrameLayout.LayoutParams(-1,-1));
    LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
    View space=new View(this);header.addView(space,new LinearLayout.LayoutParams(0,dp(38),1));
    TextClock clock=new TextClock(this);clock.setTimeZone("Asia/Kolkata");clock.setFormat12Hour("h:mm a");clock.setFormat24Hour("HH:mm");clock.setTextSize(14);clock.setTextColor(0xFFD8E1F7);clock.setPadding(0,0,dp(20),0);header.addView(clock);
    TextView sleep=text("Sleep",14,Color.WHITE);sleep.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.sleep_moon,0,0,0);sleep.setCompoundDrawablePadding(dp(7));sleep.setGravity(Gravity.CENTER);sleep.setPadding(dp(14),dp(8),dp(14),dp(8));sleep.setBackground(rounded(0x25FFFFFF,22));sleep.setFocusable(true);sleep.setId(View.generateViewId());sleep.setContentDescription("Put Fire TV to sleep");sleep.setOnFocusChangeListener((v,f)->{v.setBackground(rounded(f?0x66FFFFFF:0x25FFFFFF,22));if(f)showRow(0);});sleep.setOnClickListener(v->{if(!SleepService.sleep())new AlertDialog.Builder(this).setTitle("Sleep unavailable").setMessage("Quiet TV Sleep must be enabled in Accessibility settings to use this button.").setPositiveButton("OK",null).show();});LinearLayout.LayoutParams sleepLp=new LinearLayout.LayoutParams(-2,-2);sleepLp.setMargins(0,0,dp(10),0);header.addView(sleep,sleepLp);
    TextView settings=text("Settings",14,Color.WHITE);settings.setGravity(Gravity.CENTER);settings.setPadding(dp(14),dp(8),dp(14),dp(8));settings.setBackground(rounded(0x25FFFFFF,22));settings.setFocusable(true);settings.setId(View.generateViewId());settings.setOnClickListener(v->options());settings.setOnFocusChangeListener((v,f)->{v.setBackground(rounded(f?0x66FFFFFF:0x25FFFFFF,22));if(f)showRow(0);});header.addView(settings);
    page.addView(header,new LinearLayout.LayoutParams(-1,dp(42)));
    rowViewport=new FrameLayout(this);rowViewport.setClipChildren(true);rowViewport.setClipToPadding(true);page.addView(rowViewport,new LinearLayout.LayoutParams(-1,0,1));
    content=new LinearLayout(this);content.setOrientation(1);content.setClipChildren(false);content.setClipToPadding(false);activeRow=-1;
    ArrayList<Entry> visible=new ArrayList<>();Set<String> hidden=prefs.getStringSet("hidden",Collections.emptySet());for(Entry e:apps)if(!hidden.contains(e.pkg))visible.add(e);
    visible.add(new Entry("settings","Fire TV Settings",new Intent(android.provider.Settings.ACTION_SETTINGS),null));
    int cell=(width-100)/6;first=null;View remembered=null;
    int height=(int)(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density);
    int rowHeight=(int)((cell-24)*.60f+20)+24;
    rowStride=dp(rowHeight+10);
    FrameLayout.LayoutParams contentLp=new FrameLayout.LayoutParams(-1,((visible.size()+5)/6)*rowStride);
    contentLp.topMargin=dp(height-22-42-12-rowHeight-24);
    rowViewport.addView(content,contentLp);
    ArrayList<View> tileViews=new ArrayList<>();
    for(int row=0;row<(visible.size()+5)/6;row++){
      LinearLayout line=new LinearLayout(this);line.setOrientation(0);line.setClipChildren(false);line.setClipToPadding(false);line.setGravity(Gravity.CENTER_HORIZONTAL);line.setPadding(dp(12),dp(12),dp(12),dp(12));
      if(row==0)line.setBackground(glass());
      for(int col=0;col<6&&row*6+col<visible.size();col++){
        Entry e=visible.get(row*6+col);View tile=tile(e,cell);line.addView(tile,new LinearLayout.LayoutParams(dp(cell),dp((cell-24)*.60f+20)));tile.setTag(row*6+col);tileViews.add(tile);if(first==null)first=tile;if(e.pkg.equals(focused))remembered=tile;
      }
      LinearLayout.LayoutParams rowLp=new LinearLayout.LayoutParams(-1,-2);rowLp.bottomMargin=dp(10);content.addView(line,rowLp);
    }
    for(int i=0;i<tileViews.size();i++){
      View tile=tileViews.get(i);int col=i%6;
      tile.setNextFocusLeftId(tileViews.get(col==0?i:i-1).getId());
      tile.setNextFocusRightId(tileViews.get(col==5||i==tileViews.size()-1?i:i+1).getId());
      if(i>=6)tile.setNextFocusUpId(tileViews.get(i-6).getId());else tile.setNextFocusUpId(col<5?sleep.getId():settings.getId());
      tile.setNextFocusDownId(tileViews.get(i+6<tileViews.size()?i+6:i).getId());
    }
    sleep.setNextFocusDownId(tileViews.get(Math.min(4,tileViews.size()-1)).getId());settings.setNextFocusDownId(tileViews.get(Math.min(5,tileViews.size()-1)).getId());
    setContentView(root);getWindow().setBackgroundDrawable(null);if(remembered!=null)remembered.requestFocus();else if(first!=null)first.requestFocus();
  }
  void showRow(int row){
    if(content==null||row==activeRow)return;
    boolean initial=activeRow<0;activeRow=row;
    content.animate().cancel();
    // Keep future rows hidden even with the tighter spacing at the screen edge.
    for(int i=0;i<content.getChildCount();i++){
      View line=content.getChildAt(i);line.animate().cancel();float alpha=i<=row?1f:0f;
      if(initial||!prefs.getBoolean("motion",true))line.setAlpha(alpha);
      else line.animate().alpha(alpha).setDuration(220).setInterpolator(FOCUS_EASE).start();
    }
    float target=-row*rowStride;
    if(initial||!prefs.getBoolean("motion",true)){content.setTranslationY(target);return;}
    content.animate().translationY(target).setDuration(220).setInterpolator(FOCUS_EASE).start();
  }
  Drawable glass(){
    GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x55ECF4FF,0x253B546E,0x44344052});d.setCornerRadius(dp(30));d.setStroke(dp(1),0x66FFFFFF);return d;
  }
  View tile(Entry e,int cell){
    LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(12),dp(10),dp(12),0);box.setClipChildren(false);box.setClipToPadding(false);box.setFocusable(true);box.setClickable(true);box.setContentDescription(e.label);box.setId(View.generateViewId());
    FrameLayout card=new FrameLayout(this);card.setBackground(rounded(0xFF202B45,14));card.setClipToOutline(true);card.setElevation(0);card.setLayerType(View.LAYER_TYPE_HARDWARE,null);
    int h=dp((cell-24)*.60f);
    FrameLayout stage=new FrameLayout(this);stage.setClipChildren(false);stage.setClipToPadding(false);
    box.addView(stage,new LinearLayout.LayoutParams(-1,h));
    stage.addView(card,new FrameLayout.LayoutParams(-1,-1));
    if(e.art!=null){ImageView image=new ImageView(this);image.setImageDrawable(e.art);boolean wide=e.art.getIntrinsicWidth()>e.art.getIntrinsicHeight()*1.3;image.setScaleType(wide?ImageView.ScaleType.CENTER_CROP:ImageView.ScaleType.FIT_CENTER);if(!wide)image.setPadding(dp(17),dp(10),dp(17),dp(10));card.addView(image,new FrameLayout.LayoutParams(-1,-1));}
    else {TextView icon=text("⚙",38,0xFFE5EAF6);icon.setGravity(Gravity.CENTER);card.addView(icon,new FrameLayout.LayoutParams(-1,-1));}
    final android.animation.ValueAnimator focusAnimation=new android.animation.ValueAnimator();
    final float[] focusProgress={0f};
    focusAnimation.setInterpolator(FOCUS_EASE);
    focusAnimation.addUpdateListener(value->{
        float progress=(float)value.getAnimatedValue();focusProgress[0]=progress;
        stage.setScaleX(1f+.20f*progress);stage.setScaleY(1f+.20f*progress);stage.setTranslationY(-dp(2)*progress);
    });
    box.setOnFocusChangeListener((v,f)->{
      focusAnimation.cancel();focusAnimation.setFloatValues(focusProgress[0],f?1f:0f);
      focusAnimation.setDuration(prefs.getBoolean("motion",true)?(f?220:180):0);focusAnimation.start();
      box.setTranslationZ(f?dp(8):0);
      if(f){focused=e.pkg;if(box.getTag() instanceof Integer)showRow(((Integer)box.getTag())/6);}
    });
    card.post(()->{if(!isDestroyed()&&card.isAttachedToWindow()&&card.getWidth()>0){card.buildLayer();cacheLaunchArtwork(e,card);}});
    box.setOnClickListener(v->launch(e,card));box.setOnLongClickListener(v->{if(!e.pkg.equals("settings"))manage(e);else options();return true;});return box;
  }
  void clearLaunch(){
    launching=false;
    if(launchAnimation!=null){launchAnimation.cancel();launchAnimation=null;}
    if(launchOverlay!=null){launchOverlay.animate().cancel();homeRoot.removeView(launchOverlay);launchOverlay=null;}
    if(launchSource!=null){launchSource.setAlpha(1f);launchSource=null;}
    if(homePage!=null){homePage.animate().cancel();homePage.setAlpha(1f);}
  }
  Bitmap cacheLaunchArtwork(Entry e,View source){
    Bitmap snapshot=launchArtwork.get(e.pkg);
    if(snapshot!=null&&snapshot.getWidth()==source.getWidth()&&snapshot.getHeight()==source.getHeight())return snapshot;
    snapshot=Bitmap.createBitmap(source.getWidth(),source.getHeight(),Bitmap.Config.ARGB_8888);
    source.draw(new Canvas(snapshot));snapshot.prepareToDraw();launchArtwork.put(e.pkg,snapshot);return snapshot;
  }
  Drawable launchBackdrop(Bitmap artwork){
    // Sample all four edges. Flat artwork gets a matching solid; colorful art gets a soft full-art gradient.
    int w=artwork.getWidth(),h=artwork.getHeight(),inset=Math.max(2,Math.min(w,h)/7);
    int[] colors={artwork.getPixel(inset,inset),artwork.getPixel(w-inset-1,inset),artwork.getPixel(inset,h-inset-1),artwork.getPixel(w-inset-1,h-inset-1)};
    int r=0,g=0,b=0;for(int color:colors){r+=Color.red(color);g+=Color.green(color);b+=Color.blue(color);}r/=4;g/=4;b/=4;
    int variance=0;for(int color:colors)variance+=Math.abs(Color.red(color)-r)+Math.abs(Color.green(color)-g)+Math.abs(Color.blue(color)-b);
    if(variance<120)return new ColorDrawable(Color.rgb(r,g,b));
    Bitmap soft=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888);
    for(int y=0;y<24;y++)for(int x=0;x<24;x++){
      float fx=x/23f,fy=y/23f;float[] weight={(1-fx)*(1-fy),fx*(1-fy),(1-fx)*fy,fx*fy};
      float red=0,green=0,blue=0;for(int i=0;i<4;i++){red+=Color.red(colors[i])*weight[i];green+=Color.green(colors[i])*weight[i];blue+=Color.blue(colors[i])*weight[i];}
      soft.setPixel(x,y,Color.rgb((int)red,(int)green,(int)blue));
    }soft.prepareToDraw();
    BitmapDrawable gradient=new BitmapDrawable(getResources(),soft);gradient.setGravity(Gravity.FILL);gradient.setFilterBitmap(true);return gradient;
  }
  void launch(Entry e){launch(e,null);}
  void launch(Entry e,View source){
    if(launching)return;
    if(source==null||!prefs.getBoolean("motion",true)||source.getWidth()==0){openApp(e);return;}
    // Animate our own artwork before handing control to the external app window.
    Bitmap snapshot=cacheLaunchArtwork(e,source);
    int[] origin=new int[2],rootOrigin=new int[2];source.getLocationOnScreen(origin);homeRoot.getLocationOnScreen(rootOrigin);
    FrameLayout tile=new FrameLayout(this);
    ImageView logo=new ImageView(this);logo.setImageBitmap(snapshot);logo.setScaleType(ImageView.ScaleType.FIT_XY);
    logo.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View view,Outline outline){outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),dp(14));}});
    logo.setClipToOutline(true);
    tile.addView(logo,new FrameLayout.LayoutParams(-1,-1));
    tile.setBackground(launchBackdrop(snapshot));tile.setClipToOutline(true);
    final float[] corner={dp(14)};
    tile.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View view,Outline outline){outline.setRoundRect(0,0,view.getWidth(),view.getHeight(),corner[0]);}});
    FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(source.getWidth(),source.getHeight());
    float initialScale=((View)source.getParent()).getScaleX();
    lp.leftMargin=origin[0]-rootOrigin[0]+Math.round(source.getWidth()*(initialScale-1f)*.5f);lp.topMargin=origin[1]-rootOrigin[1]+Math.round(source.getHeight()*(initialScale-1f)*.5f);
    homeRoot.addView(tile,lp);launchOverlay=tile;launchSource=source;launching=true;source.setAlpha(0f);
    float targetX=homeRoot.getWidth()*.5f-(lp.leftMargin+source.getWidth()*.5f);
    float targetY=homeRoot.getHeight()*.5f-(lp.topMargin+source.getHeight()*.5f);
    float fullScale=Math.max((float)homeRoot.getWidth()/source.getWidth(),(float)homeRoot.getHeight()/source.getHeight());
    tile.setScaleX(initialScale);tile.setScaleY(initialScale);
    launchAnimation=android.animation.ValueAnimator.ofFloat(0f,1f);
    launchAnimation.setDuration(420);launchAnimation.setInterpolator(new PathInterpolator(.32f,0f,.2f,1f));
    launchAnimation.addUpdateListener(value->{
      float progress=(float)value.getAnimatedValue();float scale=initialScale+(fullScale-initialScale)*progress;
      tile.setScaleX(scale);tile.setScaleY(scale);tile.setTranslationX(targetX*progress);tile.setTranslationY(targetY*progress);
      // Expand the card surface, not the low-resolution installed app banner.
      logo.setScaleX(initialScale/scale);logo.setScaleY(initialScale/scale);
      corner[0]=dp(14)*(1f-progress);tile.invalidateOutline();
    });
    launchAnimation.addListener(new android.animation.AnimatorListenerAdapter(){
      boolean cancelled;
      @Override public void onAnimationCancel(android.animation.Animator animation){cancelled=true;}
      @Override public void onAnimationEnd(android.animation.Animator animation){if(!cancelled&&launching&&active)openApp(e);}
    });
    launchAnimation.start();
  }
  void openApp(Entry e){
    try{
      if(prefs.getBoolean("motion",true))startActivity(e.intent,ActivityOptions.makeCustomAnimation(this,R.anim.app_enter,R.anim.home_exit).toBundle());
      else {startActivity(e.intent);overridePendingTransition(0,0);}
    }catch(Exception ex){clearLaunch();Toast.makeText(this,"Couldn’t open "+e.label,Toast.LENGTH_SHORT).show();}
  }
  void manage(Entry e){new AlertDialog.Builder(this).setTitle(e.label).setItems(new String[]{"Open","Hide from Home","App settings"},(d,n)->{if(n==0)launch(e);if(n==1){Set<String> h=new HashSet<>(prefs.getStringSet("hidden",Collections.emptySet()));h.add(e.pkg);prefs.edit().putStringSet("hidden",h).apply();build();}if(n==2){try{startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+e.pkg)));}catch(Exception ex){launch(new Entry("","Settings",new Intent(android.provider.Settings.ACTION_SETTINGS),null));}}}).show();}
  void options(){new AlertDialog.Builder(this).setTitle("Quiet TV").setItems(new String[]{"Fire TV Settings","Background","Smooth focus motion: "+(prefs.getBoolean("motion",true)?"on":"off"),"Show all hidden apps","Refresh apps","Home button setup"},(d,n)->{
    if(n==0)launch(new Entry("","Settings",new Intent(android.provider.Settings.ACTION_SETTINGS),null));
    if(n==1)new AlertDialog.Builder(this).setTitle("Background").setItems(new String[]{"Rotate 22 wallpapers · every 10 seconds","Choose one wallpaper","Twilight gradient","Ocean gradient","Graphite gradient"},(dlg,x)->{
      if(x==1)new AlertDialog.Builder(this).setTitle("Choose wallpaper").setItems(wallpaperNames,(pick,index)->{prefs.edit().putInt("palette",10+index).apply();build();}).show();
      else {prefs.edit().putInt("palette",x==0?4:x-2).apply();build();}
    }).show();
    if(n==2)prefs.edit().putBoolean("motion",!prefs.getBoolean("motion",true)).apply();
    if(n==3){prefs.edit().remove("hidden").apply();build();}
    if(n==4){discover();build();}
    if(n==5){Intent i=getPackageManager().getLaunchIntentForPackage("io.github.toolicious.homeonfire");if(i!=null)startActivity(i);else Toast.makeText(this,"Home button helper is not installed",Toast.LENGTH_SHORT).show();}
  }).show();}
  class Wallpaper extends View {
    Bitmap cache,next;final Paint paint=new Paint(Paint.FILTER_BITMAP_FLAG);final Rect destination=new Rect();final RectF slideCurrent=new RectF(),slideNext=new RectF();
    android.animation.ValueAnimator fade;float blend;boolean loading;int generation;
    final Runnable rotate=()->loadNext();
    Wallpaper(Context c){super(c);}
    @Override protected void onSizeChanged(int w,int h,int ow,int oh){
      destination.set(0,0,w,h);int palette=prefs.getInt("palette",4);
      if(wallpaperCache!=null&&wallpaperPalette==palette){cache=wallpaperCache;startRotation();return;}
      loadFrame(palette==4?rotationIndex:palette,false);
    }
    void startRotation(){handler.removeCallbacks(rotate);if(active&&cache!=null&&prefs.getInt("palette",4)==4)handler.postDelayed(rotate,8600);}
    void stopRotation(){handler.removeCallbacks(rotate);if(fade!=null){fade.removeAllListeners();fade.cancel();fade=null;}if(next!=null){cache=next;wallpaperCache=cache;next=null;}blend=0;invalidate();}
    @Override protected void onDetachedFromWindow(){stopRotation();generation++;super.onDetachedFromWindow();}
    void loadNext(){if(!active||loading||prefs.getInt("palette",4)!=4)return;rotationIndex=(rotationIndex+1)%wallpaperResources.length;loadFrame(rotationIndex,true);}
    void loadFrame(int choice,boolean transition){
      if(getWidth()==0||getHeight()==0||wallpaperWorker.isShutdown())return;
      loading=true;final int token=++generation,palette=prefs.getInt("palette",4),w=getWidth(),h=getHeight();
      final int actual=palette==4?10+choice:choice;
      wallpaperWorker.execute(()->{
        Bitmap frame=renderFrame(w,h,actual);frame.prepareToDraw();
        handler.post(()->{
          if(token!=generation||isDestroyed())return;loading=false;
          if(transition&&active&&cache!=null&&prefs.getBoolean("motion",true)){
            next=frame;fade=android.animation.ValueAnimator.ofFloat(0,1);fade.setDuration(1400);fade.setInterpolator(FOCUS_EASE);
            fade.addUpdateListener(v->{blend=(float)v.getAnimatedValue();invalidate();});
            fade.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator animation){cache=frame;wallpaperCache=frame;wallpaperPalette=palette;next=null;blend=0;invalidate();startRotation();}});fade.start();
          }else{cache=frame;wallpaperCache=frame;wallpaperPalette=palette;invalidate();startRotation();}
        });
      });
    }
    Bitmap renderFrame(int w,int h,int palette){
      Bitmap frame=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);frame.setHasAlpha(false);Canvas c=new Canvas(frame);Paint p=new Paint(Paint.FILTER_BITMAP_FLAG);
      if(palette>=3){
        BitmapFactory.Options opts=new BitmapFactory.Options();opts.inScaled=false;
        int res=wallpaperResources[Math.max(0,Math.min(wallpaperResources.length-1,palette-10))];
        Bitmap photo=BitmapFactory.decodeResource(getResources(),res,opts);
        float scale=Math.max((float)w/photo.getWidth(),(float)h/photo.getHeight());float pw=photo.getWidth()*scale,ph=photo.getHeight()*scale;
        c.drawBitmap(photo,null,new RectF((w-pw)/2,(h-ph)/2,(w+pw)/2,(h+ph)/2),p);photo.recycle();
        c.drawColor(0x22030D22);p.setShader(new LinearGradient(0,0,0,h,new int[]{0x33000A1B,0x00000A1B,0x77000A1B},new float[]{0,.35f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
      }else{
        int[] colors=palette==1?new int[]{0xFF103C50,0xFF133047,0xFF081B2D}:palette==2?new int[]{0xFF333844,0xFF202430,0xFF11141D}:new int[]{0xFF343F73,0xFF202D53,0xFF101629};
        p.setShader(new LinearGradient(0,0,w,h,colors,null,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
      }return frame;
    }
    @Override public boolean isOpaque(){return true;}
    @Override protected void onDraw(Canvas c){
      if(cache==null){c.drawColor(0xFF101629);return;}
      paint.setAlpha(255);
      if(next!=null&&blend>0){
        // Small opposing drifts with overscan keep every edge covered while blending.
        float w=getWidth(),h=getHeight(),out=blend,in=1f-blend;
        float outX=w*.03f*out,outY=h*.03f*out,inX=w*.03f*in,inY=h*.03f*in;
        float drift=w*.025f;
        slideCurrent.set(-outX-drift*out,-outY,w+outX-drift*out,h+outY);
        slideNext.set(-inX+drift*in,-inY,w+inX+drift*in,h+inY);
        // Keep the outgoing image opaque underneath: no black dip halfway through.
        c.drawBitmap(cache,null,slideCurrent,paint);
        paint.setAlpha(Math.round(255*blend));c.drawBitmap(next,null,slideNext,paint);paint.setAlpha(255);
      }else c.drawBitmap(cache,null,destination,paint);
    }
  }
}
