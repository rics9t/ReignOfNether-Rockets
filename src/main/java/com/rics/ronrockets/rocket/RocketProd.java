/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/rocket/RocketProd.java:24: error: constructor ResourceCost in class ResourceCost cannot be applied to given types;
    public final static ResourceCost cost = new ResourceCost(0, 500, 1000, 120, 0);
                                            ^
  required: int,int,int,int,int
  found:    int,int,int,int,int
  reason: ResourceCost(int,int,int,int,int) has private access in ResourceCost
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/rocket/RocketProd.java:38: error: method does not override or implement a method from a supertype
    @Override
    ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/rocket/RocketProd.java:42: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/rocket/RocketProd.java:58: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/rocket/RocketProd.java:76: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/RonRocketsMod.java:21: warning: [removal] get() in FMLJavaModLoadingContext has been deprecated and marked for removal
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
                                                   ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/client/RocketRenderer.java:47: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
        return new ResourceLocation("ronrockets", "textures/entity/rocket.png");
               ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/ability/ProduceRocketAbility.java:30: error: cannot find symbol
        int prod = RocketManager.productionTicks.getOrDefault(pos, 0);
                                ^
  symbol:   variable productionTicks
  location: class RocketManager
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/ability/ProduceRocketAbility.java:50: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/produce_rocket.png"),
                ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/ability/ProduceRocketAbility.java:69: error: cannot find symbol
            && RocketManager.productionTicks.getOrDefault(pos, 0) <= 0 
                            ^
  symbol:   variable productionTicks
  location: class RocketManager
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/ability/ProduceRocketAbility.java:72: error: cannot find symbol
            RocketManager.productionTicks.put(pos, 2400); // ✅ Starts 2 Minute Creation
                         ^
  symbol:   variable productionTicks
  location: class RocketManager
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/ability/LaunchRocketAbility.java:37: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/launch_rocket.png"),
                ^
/home/runner/work/ReignOfNether-Rockets/ReignOfNether-Rockets/src/main/java/com/rics/ronrockets/ability/ShieldInterceptAbility.java:34: warning: [removal] ResourceLocation(String,String) in ResourceLocation has been deprecated and marked for removal
                new ResourceLocation(RonRocketsMod.MODID, "textures/icons/shield_intercept.png"),
                ^
Note: Some messages have been simplified; recompile with -Xdiags:verbose to get full output
5 errors
