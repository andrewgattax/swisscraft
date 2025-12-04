package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.RecoverWorldDataScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.util.MemoryReserve;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldOpenFlows {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final UUID WORLD_PACK_ID = UUID.fromString("640a6a92-b6cb-48a0-b391-831586500359");
   private final Minecraft minecraft;
   private final LevelStorageSource levelSource;

   public WorldOpenFlows(Minecraft p_233093_, LevelStorageSource p_233094_) {
      this.minecraft = p_233093_;
      this.levelSource = p_233094_;
   }

   public void createFreshLevel(String p_233158_, LevelSettings p_233159_, WorldOptions p_249243_, Function<RegistryAccess, WorldDimensions> p_249252_, Screen p_310233_) {
      this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
      LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.createWorldAccess(p_233158_);
      if (levelstoragesource$levelstorageaccess != null) {
         PackRepository packrepository = ServerPacksSource.createPackRepository(levelstoragesource$levelstorageaccess);
         WorldDataConfiguration worlddataconfiguration = p_233159_.getDataConfiguration();

         try {
            WorldLoader.PackConfig worldloader$packconfig = new WorldLoader.PackConfig(packrepository, worlddataconfiguration, false, false);
            WorldStem worldstem = this.loadWorldDataBlocking(worldloader$packconfig, (p_258145_) -> {
               WorldDimensions.Complete worlddimensions$complete = p_249252_.apply(p_258145_.datapackWorldgen()).bake(p_258145_.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
               return new WorldLoader.DataLoadOutput<>(new PrimaryLevelData(p_233159_, p_249243_, worlddimensions$complete.specialWorldProperty(), worlddimensions$complete.lifecycle()), worlddimensions$complete.dimensionsRegistryAccess());
            }, WorldStem::new);
            this.minecraft.doWorldLoad(levelstoragesource$levelstorageaccess, packrepository, worldstem, true);
         } catch (Exception exception) {
            LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)exception);
            levelstoragesource$levelstorageaccess.safeClose();
            this.minecraft.setScreen(p_310233_);
         }

      }
   }

   @Nullable
   private LevelStorageSource.LevelStorageAccess createWorldAccess(String p_233156_) {
      try {
         return this.levelSource.validateAndCreateAccess(p_233156_);
      } catch (IOException ioexception) {
         LOGGER.warn("Failed to read level {} data", p_233156_, ioexception);
         SystemToast.onWorldAccessFailure(this.minecraft, p_233156_);
         this.minecraft.setScreen((Screen)null);
         return null;
      } catch (ContentValidationException contentvalidationexception) {
         LOGGER.warn("{}", (Object)contentvalidationexception.getMessage());
         this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> {
            this.minecraft.setScreen((Screen)null);
         }));
         return null;
      }
   }

   public void createLevelFromExistingSettings(LevelStorageSource.LevelStorageAccess p_250919_, ReloadableServerResources p_248897_, LayeredRegistryAccess<RegistryLayer> p_250801_, WorldData p_251654_) {
      PackRepository packrepository = ServerPacksSource.createPackRepository(p_250919_);
      CloseableResourceManager closeableresourcemanager = (new WorldLoader.PackConfig(packrepository, p_251654_.getDataConfiguration(), false, false)).createResourceManager().getSecond();
      this.minecraft.doWorldLoad(p_250919_, packrepository, new WorldStem(closeableresourcemanager, p_248897_, p_250801_, p_251654_), true);
   }

   public WorldStem loadWorldStem(Dynamic<?> p_312184_, boolean p_233124_, PackRepository p_233125_) throws Exception {
      WorldLoader.PackConfig worldloader$packconfig = LevelStorageSource.getPackConfig(p_312184_, p_233125_, p_233124_);
      return this.loadWorldDataBlocking(worldloader$packconfig, (p_308270_) -> {
         Registry<LevelStem> registry = p_308270_.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
         LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(p_312184_, p_308270_.dataConfiguration(), registry, p_308270_.datapackWorldgen());
         return new WorldLoader.DataLoadOutput<>(leveldataanddimensions.worldData(), leveldataanddimensions.dimensions().dimensionsRegistryAccess());
      }, WorldStem::new);
   }

   public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess p_249540_) throws Exception {
      PackRepository packrepository = ServerPacksSource.createPackRepository(p_249540_);
      Dynamic<?> dynamic = p_249540_.getDataTag();
      WorldLoader.PackConfig worldloader$packconfig = LevelStorageSource.getPackConfig(dynamic, packrepository, false);
         @OnlyIn(Dist.CLIENT)
         record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
         }
      return this.<Data, Pair<LevelSettings, WorldCreationContext>>loadWorldDataBlocking(worldloader$packconfig, (p_308268_) -> {
         Registry<LevelStem> registry = (new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable())).freeze();
         LevelDataAndDimensions leveldataanddimensions = LevelStorageSource.getLevelDataAndDimensions(dynamic, p_308268_.dataConfiguration(), registry, p_308268_.datapackWorldgen());
         return new WorldLoader.DataLoadOutput<>(new Data(leveldataanddimensions.worldData().getLevelSettings(), leveldataanddimensions.worldData().worldGenOptions(), leveldataanddimensions.dimensions().dimensions()), p_308268_.datapackDimensions());
      }, (p_247840_, p_247841_, p_247842_, p_247843_) -> {
         p_247840_.close();
         return Pair.of(p_247843_.levelSettings, new WorldCreationContext(p_247843_.options, new WorldDimensions(p_247843_.existingDimensions), p_247842_, p_247841_, p_247843_.levelSettings.getDataConfiguration()));
      });
   }

   private <D, R> R loadWorldDataBlocking(WorldLoader.PackConfig p_250997_, WorldLoader.WorldDataSupplier<D> p_251759_, WorldLoader.ResultFactory<D, R> p_249635_) throws Exception {
      WorldLoader.InitConfig worldloader$initconfig = new WorldLoader.InitConfig(p_250997_, Commands.CommandSelection.INTEGRATED, 2);
      CompletableFuture<R> completablefuture = WorldLoader.load(worldloader$initconfig, p_251759_, p_249635_, Util.backgroundExecutor(), this.minecraft);
      this.minecraft.managedBlock(completablefuture::isDone);
      return completablefuture.get();
   }

   private void askForBackup(LevelStorageSource.LevelStorageAccess p_312560_, boolean p_233143_, Runnable p_233144_, Runnable p_312163_) {
      Component component;
      Component component1;
      if (p_233143_) {
         component = Component.translatable("selectWorld.backupQuestion.customized");
         component1 = Component.translatable("selectWorld.backupWarning.customized");
      } else {
         component = Component.translatable("selectWorld.backupQuestion.experimental");
         component1 = Component.translatable("selectWorld.backupWarning.experimental");
      }

      this.minecraft.setScreen(new BackupConfirmScreen(p_312163_, (p_308273_, p_308274_) -> {
         if (p_308273_) {
            EditWorldScreen.makeBackupAndShowToast(p_312560_);
         }

         p_233144_.run();
      }, component, component1, false));
   }

   public static void confirmWorldCreation(Minecraft p_270593_, CreateWorldScreen p_270733_, Lifecycle p_270539_, Runnable p_270158_, boolean p_270709_) {
      BooleanConsumer booleanconsumer = (p_233154_) -> {
         if (p_233154_) {
            p_270158_.run();
         } else {
            p_270593_.setScreen(p_270733_);
         }

      };
      if (!p_270709_ && p_270539_ != Lifecycle.stable()) {
         if (p_270539_ == Lifecycle.experimental()) {
            p_270593_.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.warning.experimental.title"), Component.translatable("selectWorld.warning.experimental.question")));
         } else {
            p_270593_.setScreen(new ConfirmScreen(booleanconsumer, Component.translatable("selectWorld.warning.deprecated.title"), Component.translatable("selectWorld.warning.deprecated.question")));
         }
      } else {
         p_270158_.run();
      }

   }

   public void checkForBackupAndLoad(String p_310959_, Runnable p_309850_) {
      this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
      LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = this.createWorldAccess(p_310959_);
      if (levelstoragesource$levelstorageaccess != null) {
         this.checkForBackupAndLoad(levelstoragesource$levelstorageaccess, p_309850_);
      }
   }

   private void checkForBackupAndLoad(LevelStorageSource.LevelStorageAccess p_310121_, Runnable p_309476_) {
      this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));

      Dynamic<?> dynamic;
      LevelSummary levelsummary;
      try {
         dynamic = p_310121_.getDataTag();
         levelsummary = p_310121_.getSummary(dynamic);
      } catch (NbtException | ReportedNbtException | IOException ioexception) {
         this.minecraft.setScreen(new RecoverWorldDataScreen(this.minecraft, (p_308264_) -> {
            if (p_308264_) {
               this.checkForBackupAndLoad(p_310121_, p_309476_);
            } else {
               p_310121_.safeClose();
               p_309476_.run();
            }

         }, p_310121_));
         return;
      } catch (OutOfMemoryError outofmemoryerror1) {
         MemoryReserve.release();
         System.gc();
         String s = "Ran out of memory trying to read level data of world folder \"" + p_310121_.getLevelId() + "\"";
         LOGGER.error(LogUtils.FATAL_MARKER, s);
         OutOfMemoryError outofmemoryerror = new OutOfMemoryError("Ran out of memory reading level data");
         outofmemoryerror.initCause(outofmemoryerror1);
         CrashReport crashreport = CrashReport.forThrowable(outofmemoryerror, s);
         CrashReportCategory crashreportcategory = crashreport.addCategory("World details");
         crashreportcategory.setDetail("World folder", p_310121_.getLevelId());
         throw new ReportedException(crashreport);
      }

      if (!levelsummary.isCompatible()) {
         p_310121_.safeClose();
         this.minecraft.setScreen(new AlertScreen(p_309476_, Component.translatable("selectWorld.incompatible.title").withColor(-65536), Component.translatable("selectWorld.incompatible.description", levelsummary.getWorldVersionName())));
      } else {
         LevelSummary.BackupStatus levelsummary$backupstatus = levelsummary.backupStatus();
         if (levelsummary$backupstatus.shouldBackup()) {
            String s1 = "selectWorld.backupQuestion." + levelsummary$backupstatus.getTranslationKey();
            String s2 = "selectWorld.backupWarning." + levelsummary$backupstatus.getTranslationKey();
            MutableComponent mutablecomponent = Component.translatable(s1);
            if (levelsummary$backupstatus.isSevere()) {
               mutablecomponent.withColor(-2142128);
            }

            Component component = Component.translatable(s2, levelsummary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
            this.minecraft.setScreen(new BackupConfirmScreen(() -> {
               p_310121_.safeClose();
               p_309476_.run();
            }, (p_308256_, p_308257_) -> {
               if (p_308256_) {
                  EditWorldScreen.makeBackupAndShowToast(p_310121_);
               }

               this.loadLevel(p_310121_, dynamic, false, true, p_309476_);
            }, mutablecomponent, component, false));
         } else {
            this.loadLevel(p_310121_, dynamic, false, true, p_309476_);
         }

      }
   }

   public CompletableFuture<Void> loadBundledResourcePack(DownloadedPackSource p_312230_, LevelStorageSource.LevelStorageAccess p_310544_) {
      Path path = p_310544_.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
      if (Files.exists(path) && !Files.isDirectory(path)) {
         p_312230_.configureForLocalWorld();
         CompletableFuture<Void> completablefuture = p_312230_.waitForPackFeedback(WORLD_PACK_ID);
         p_312230_.pushLocalPack(WORLD_PACK_ID, path);
         return completablefuture;
      } else {
         return CompletableFuture.completedFuture((Void)null);
      }
   }

   private void loadLevel(LevelStorageSource.LevelStorageAccess p_309733_, Dynamic<?> p_312455_, boolean p_310657_, boolean p_309504_, Runnable p_309915_) {
      loadLevel(p_309733_, p_312455_, p_310657_, p_309504_, p_309915_, false);
   }

   private void loadLevel(LevelStorageSource.LevelStorageAccess p_309733_, Dynamic<?> p_312455_, boolean p_310657_, boolean p_309504_, Runnable p_309915_, boolean confirmExperimentalWarning) {
      this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.resource_load")));
      PackRepository packrepository = ServerPacksSource.createPackRepository(p_309733_);

      net.minecraftforge.common.ForgeHooks.readAdditionalLevelSaveData(p_312455_, p_309733_.getLevelDirectory());

      WorldStem worldstem;
      try {
         worldstem = this.loadWorldStem(p_312455_, p_310657_, packrepository);
         if (confirmExperimentalWarning && worldstem.worldData() instanceof PrimaryLevelData pld) pld.withConfirmedWarning(true);
      } catch (Exception exception) {
         LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", (Throwable)exception);
         if (!p_310657_) {
            this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> {
               p_309733_.safeClose();
               p_309915_.run();
            }, () -> {
               this.loadLevel(p_309733_, p_312455_, true, p_309504_, p_309915_);
            }));
         } else {
            p_309733_.safeClose();
            this.minecraft.setScreen(new AlertScreen(p_309915_, Component.translatable("datapackFailure.safeMode.failed.title"), Component.translatable("datapackFailure.safeMode.failed.description"), CommonComponents.GUI_BACK, true));
         }

         return;
      }

      WorldData worlddata = worldstem.worldData();
      boolean flag = worlddata.worldGenOptions().isOldCustomizedWorld();
      boolean skipConfirmation = worlddata instanceof PrimaryLevelData pld && pld.hasConfirmedExperimentalWarning();
      boolean flag1 = worlddata.worldGenSettingsLifecycle() != Lifecycle.stable() && !skipConfirmation;
      if (!p_309504_ || !flag && !flag1) {
         DownloadedPackSource downloadedpacksource = this.minecraft.getDownloadedPackSource();
         this.loadBundledResourcePack(downloadedpacksource, p_309733_).thenApply((p_233177_) -> {
            return true;
         }).exceptionallyComposeAsync((p_233183_) -> {
            LOGGER.warn("Failed to load pack: ", p_233183_);
            return this.promptBundledPackLoadFailure();
         }, this.minecraft).thenAcceptAsync((p_308248_) -> {
            if (p_308248_) {
               this.minecraft.doWorldLoad(p_309733_, packrepository, worldstem, false);
            } else {
               worldstem.close();
               p_309733_.safeClose();
               downloadedpacksource.popAll();
               p_309915_.run();
            }

         }, this.minecraft).exceptionally((p_233175_) -> {
            this.minecraft.delayCrash(CrashReport.forThrowable(p_233175_, "Load world"));
            return null;
         });
      } else {
         if (flag) // Forge: For legacy world options, let vanilla handle it.
         this.askForBackup(p_309733_, flag, () -> {
            this.loadLevel(p_309733_, p_312455_, p_310657_, false, p_309915_);
         }, () -> {
            p_309733_.safeClose();
            p_309915_.run();
         });
         else net.minecraftforge.client.ForgeHooksClient.createWorldConfirmationScreen(() -> this.loadLevel(p_309733_, p_312455_, p_310657_, false, p_309915_, true));
         worldstem.close();
      }
   }

   private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
      CompletableFuture<Boolean> completablefuture = new CompletableFuture<>();
      this.minecraft.setScreen(new ConfirmScreen(completablefuture::complete, Component.translatable("multiplayer.texturePrompt.failure.line1"), Component.translatable("multiplayer.texturePrompt.failure.line2"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
      return completablefuture;
   }
}
