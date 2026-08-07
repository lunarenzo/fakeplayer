package io.github.hello09x.fakeplayer.core.manager.invsee;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 默认实现, 无法看到装备栏
 *
 * @author tanyaofei
 * @since 2024/8/12
 **/
@Singleton
public class SimpleInvseeManagerImpl extends AbstractInvseeManager {

    @Inject
    public SimpleInvseeManagerImpl(FakeplayerManager manager, FakeplayerList fakeplayerList) {
        super(manager, fakeplayerList);
    }

    @Override
    protected @Nullable InventoryView openInventory(
            @NotNull Player viewer,
            @NotNull Player whom,
            @NotNull Component title
    ) {
        var holder = new InvseeInventoryHolder(whom.getUniqueId());
        var inv = Bukkit.createInventory(holder, 36, title);
        holder.setInventory(inv);
        inv.setContents(whom.getInventory().getStorageContents());
        return viewer.openInventory(inv);
    }


}
