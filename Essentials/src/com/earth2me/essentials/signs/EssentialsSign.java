package com.earth2me.essentials.signs;

import com.earth2me.essentials.*;
import static com.earth2me.essentials.I18n._;
import com.earth2me.essentials.utils.NumberUtil;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.ess3.api.IEssentials;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.SignBreakEvent;
import net.ess3.api.events.SignCreateEvent;
import net.ess3.api.events.SignInteractEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;


public class EssentialsSign
{
	private static final Set<Material> EMPTY_SET = new HashSet<Material>();
	protected static final BigDecimal MINTRANSACTION = new BigDecimal("0.01");
	protected transient final String signName;

	public EssentialsSign(final String signName)
	{
		this.signName = signName;
	}

	protected final boolean onSignCreate(final SignChangeEvent event, final IEssentials ess)
	{
		final ISign sign = new EventSign(event);
		final User user = ess.getUser(event.getPlayer());
		if (!(user.isAuthorized("essentials.signs." + signName.toLowerCase(Locale.ENGLISH) + ".create")
			  || user.isAuthorized("essentials.signs.create." + signName.toLowerCase(Locale.ENGLISH))))
		{
			// Return true, so other plugins can use the same sign title, just hope
			// they won't change it to §1[Signname]
			return true;
		}
		sign.setLine(0, _("signFormatFail", this.signName));

		final SignCreateEvent signEvent = new SignCreateEvent(sign, this, user);
		ess.getServer().getPluginManager().callEvent(signEvent);
		if (signEvent.isCancelled())
		{
			return false;
		}

		try
		{
			final boolean ret = onSignCreate(sign, user, getUsername(user), ess);
			if (ret)
			{
				sign.setLine(0, getSuccessName());
			}
			return ret;
		}
		catch (ChargeException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		catch (SignException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		// Return true, so the player sees the wrong sign.
		return true;
	}

	public String getSuccessName()
	{
		return _("signFormatSuccess", this.signName);
	}

	public String getTemplateName()
	{
		return _("signFormatTemplate", this.signName);
	}

	public String getName()
	{
		return this.signName;
	}

	public String getUsername(final User user)
	{
		return user.getName().substring(0, user.getName().length() > 13 ? 13 : user.getName().length());
	}

	protected final boolean onSignInteract(final Block block, final Player player, final IEssentials ess)
	{
		final ISign sign = new BlockSign(block);
		final User user = ess.getUser(player);
		if (user.checkSignThrottle())
		{
			return false;
		}
		try
		{
			if (user.isDead() || !(user.isAuthorized("essentials.signs." + signName.toLowerCase(Locale.ENGLISH) + ".use")
								   || user.isAuthorized("essentials.signs.use." + signName.toLowerCase(Locale.ENGLISH))))
			{
				return false;
			}

			final SignInteractEvent signEvent = new SignInteractEvent(sign, this, user);
			ess.getServer().getPluginManager().callEvent(signEvent);
			if (signEvent.isCancelled())
			{
				return false;
			}

			return onSignInteract(sign, user, getUsername(user), ess);
		}
		catch (ChargeException ex)
		{
			showError(ess, user.getSource(), ex, signName);
			return false;
		}
		catch (Exception ex)
		{
			showError(ess, user.getSource(), ex, signName);
			return false;
		}
	}

	protected final boolean onSignBreak(final Block block, final Player player, final IEssentials ess) throws MaxMoneyException
	{
		final ISign sign = new BlockSign(block);
		final User user = ess.getUser(player);
		try
		{
			if (!(user.isAuthorized("essentials.signs." + signName.toLowerCase(Locale.ENGLISH) + ".break")
				  || user.isAuthorized("essentials.signs.break." + signName.toLowerCase(Locale.ENGLISH))))
			{
				return false;
			}

			final SignBreakEvent signEvent = new SignBreakEvent(sign, this, user);
			ess.getServer().getPluginManager().callEvent(signEvent);
			if (signEvent.isCancelled())
			{
				return false;
			}

			return onSignBreak(sign, user, getUsername(user), ess);
		}
		catch (SignException ex)
		{
			showError(ess, user.getSource(), ex, signName);
			return false;
		}
	}

	protected boolean onSignCreate(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException
	{
		return true;
	}

	protected boolean onSignInteract(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException, MaxMoneyException
	{
		return true;
	}

	protected boolean onSignBreak(final ISign sign, final User player, final String username, final IEssentials ess) throws SignException, MaxMoneyException
	{
		return true;
	}

	protected final boolean onBlockPlace(final Block block, final Player player, final IEssentials ess)
	{
		User user = ess.getUser(player);
		try
		{
			return onBlockPlace(block, user, getUsername(user), ess);
		}
		catch (ChargeException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		catch (SignException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		return false;
	}

	protected final boolean onBlockInteract(final Block block, final Player player, final IEssentials ess)
	{
		User user = ess.getUser(player);
		try
		{
			return onBlockInteract(block, user, getUsername(user), ess);
		}
		catch (ChargeException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		catch (SignException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		return false;
	}

	protected final boolean onBlockBreak(final Block block, final Player player, final IEssentials ess) throws MaxMoneyException
	{
		User user = ess.getUser(player);
		try
		{
			return onBlockBreak(block, user, getUsername(user), ess);
		}
		catch (SignException ex)
		{
			showError(ess, user.getSource(), ex, signName);
		}
		return false;
	}

	protected boolean onBlockBreak(final Block block, final IEssentials ess)
	{
		return true;
	}

	protected boolean onBlockExplode(final Block block, final IEssentials ess)
	{
		return true;
	}

	protected boolean onBlockBurn(final Block block, final IEssentials ess)
	{
		return true;
	}

	protected boolean onBlockIgnite(final Block block, final IEssentials ess)
	{
		return true;
	}

	protected boolean onBlockPush(final Block block, final IEssentials ess)
	{
		return true;
	}

	protected static boolean checkIfBlockBreaksSigns(final Block block)
	{
		final Block sign = block.getRelative(BlockFace.UP);
		if (sign.getType() == Material.SIGN_POST && isValidSign(new BlockSign(sign)))
		{
			return true;
		}
		final BlockFace[] directions = new BlockFace[]
		{
			BlockFace.NORTH,
			BlockFace.EAST,
			BlockFace.SOUTH,
			BlockFace.WEST
		};
		for (BlockFace blockFace : directions)
		{
			final Block signblock = block.getRelative(blockFace);
			if (signblock.getType() == Material.WALL_SIGN)
			{
				try
				{
					final org.bukkit.material.Sign signMat = (org.bukkit.material.Sign)signblock.getState().getData();
					if (signMat != null && signMat.getFacing() == blockFace && isValidSign(new BlockSign(signblock)))
					{
						return true;
					}
				}
				catch (NullPointerException ex)
				{
					// Sometimes signs enter a state of being semi broken, having no text or state data, usually while burning.
				}
			}
		}
		return false;
	}

	public static boolean isValidSign(final ISign sign)
	{
		return sign.getLine(0).matches("§1\\[.*\\]");
	}

	protected boolean onBlockPlace(final Block block, final User player, final String username, final IEssentials ess) throws SignException, ChargeException
	{
		return true;
	}

	protected boolean onBlockInteract(final Block block, final User player, final String username, final IEssentials ess) throws SignException, ChargeException
	{
		return true;
	}

	protected boolean onBlockBreak(final Block block, final User player, final String username, final IEssentials ess) throws SignException, MaxMoneyException
	{
		return true;
	}

	public Set<Material> getBlocks()
	{
		return EMPTY_SET;
	}

	public boolean areHeavyEventRequired()
	{
		return false;
	}

	private String getSignText(final ISign sign, final int lineNumber)
	{
		return sign.getLine(lineNumber).trim();
	}

	protected final void validateTrade(final ISign sign, final int index, final IEssentials ess) throws SignException
	{
		final String line = getSignText(sign, index);
		if (line.isEmpty())
		{
			return;
		}
		final Trade trade = getTrade(sign, index, 0, ess);
		final BigDecimal money = trade.getMoney();
		if (money != null)
		{
			sign.setLine(index, NumberUtil.shortCurrency(money, ess));
		}
	}

	protected final void validateTrade(final ISign sign, final int amountIndex, final int itemIndex,
									   final User player, final IEssentials ess) throws SignException
	{
		final String itemType = getSignText(sign, itemIndex);
		if (itemType.equalsIgnoreCase("exp") || itemType.equalsIgnoreCase("xp"))
		{
			int amount = getIntegerPositive(getSignText(sign, amountIndex));
			sign.setLine(amountIndex, Integer.toString(amount));
			sign.setLine(itemIndex, "exp");
			return;
		}
		final Trade trade = getTrade(sign, amountIndex, itemIndex, player, ess);
		final ItemStack item = trade.getItemStack();
		sign.setLine(amountIndex, Integer.toString(item.getAmount()));
		sign.setLine(itemIndex, itemType);
	}

	protected final Trade getTrade(final ISign sign, final int amountIndex, final int itemIndex,
								   final User player, final IEssentials ess) throws SignException
	{
		final String itemType = getSignText(sign, itemIndex);
		if (itemType.equalsIgnoreCase("exp") || itemType.equalsIgnoreCase("xp"))
		{
			final int amount = getIntegerPositive(getSignText(sign, amountIndex));
			return new Trade(amount, ess);
		}
		final ItemStack item = getItemStack(itemType, 1, ess);
		final int amount = Math.min(getIntegerPositive(getSignText(sign, amountIndex)), item.getType().getMaxStackSize() * player.getInventory().getSize());
		if (item.getType() == Material.AIR || amount < 1)
		{
			throw new SignException(_("moreThanZero"));
		}
		item.setAmount(amount);
		return new Trade(item, ess);
	}

	protected final void validateInteger(final ISign sign, final int index) throws SignException
	{
		final String line = getSignText(sign, index);
		if (line.isEmpty())
		{
			throw new SignException("Empty line " + index);
		}
		final int quantity = getIntegerPositive(line);
		sign.setLine(index, Integer.toString(quantity));
	}

	protected final int getIntegerPositive(final String line) throws SignException
	{
		final int quantity = getInteger(line);
		if (quantity < 1)
		{
			throw new SignException(_("moreThanZero"));
		}
		return quantity;
	}

	protected final int getInteger(final String line) throws SignException
	{
		try
		{
			final int quantity = Integer.parseInt(line);

			return quantity;
		}
		catch (NumberFormatException ex)
		{
			throw new SignException("Invalid sign", ex);
		}
	}

	protected final ItemStack getItemStack(final String itemName, final int quantity, final IEssentials ess) throws SignException
	{
		try
		{
			final ItemStack item = ess.getItemDb().get(itemName);
			item.setAmount(quantity);
			return item;
		}
		catch (Exception ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}
	}

	protected final ItemStack getItemMeta(final ItemStack item, final String meta, final IEssentials ess) throws SignException
	{
		ItemStack stack = item;
		try
		{
			if (!meta.isEmpty())
			{
				MetaItemStack metaStack = new MetaItemStack(stack);
				final boolean allowUnsafe = ess.getSettings().allowUnsafeEnchantments();
				metaStack.addStringMeta(null, allowUnsafe, meta, ess);
				stack = metaStack.getItemStack();
			}
		}
		catch (Exception ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}
		return stack;
	}

	protected final BigDecimal getMoney(final String line) throws SignException
	{
		final boolean isMoney = line.matches("^[^0-9-\\.][\\.0-9]+$");
		return isMoney ? getBigDecimalPositive(line.substring(1)) : null;
	}

	protected final BigDecimal getBigDecimalPositive(final String line) throws SignException
	{
		final BigDecimal quantity = getBigDecimal(line);
		if (quantity.compareTo(MINTRANSACTION) < 0)
		{
			throw new SignException(_("moreThanZero"));
		}
		return quantity;
	}

	protected final BigDecimal getBigDecimal(final String line) throws SignException
	{
		try
		{
			return new BigDecimal(line);
		}
		catch (ArithmeticException ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}
		catch (NumberFormatException ex)
		{
			throw new SignException(ex.getMessage(), ex);
		}
	}

	protected final Trade getTrade(final ISign sign, final int index, final IEssentials ess) throws SignException
	{
		return getTrade(sign, index, 1, ess);
	}

	protected final Trade getTrade(final ISign sign, final int index, final int decrement, final IEssentials ess) throws SignException
	{
		final String line = getSignText(sign, index);
		if (line.isEmpty())
		{
			return new Trade(signName.toLowerCase(Locale.ENGLISH) + "sign", ess);
		}

		final BigDecimal money = getMoney(line);
		if (money == null)
		{
			final String[] split = line.split("[ :]+", 2);
			if (split.length != 2)
			{
				throw new SignException(_("invalidCharge"));
			}
			final int quantity = getIntegerPositive(split[0]);

			final String item = split[1].toLowerCase(Locale.ENGLISH);
			if (item.equalsIgnoreCase("times"))
			{
				sign.setLine(index, (quantity - decrement) + " times");
				sign.updateSign();
				return new Trade(signName.toLowerCase(Locale.ENGLISH) + "sign", ess);
			}
			else if (item.equalsIgnoreCase("exp") || item.equalsIgnoreCase("xp"))
			{
				sign.setLine(index, quantity + " exp");
				return new Trade(quantity, ess);
			}
			else
			{
				final ItemStack stack = getItemStack(item, quantity, ess);
				sign.setLine(index, quantity + " " + item);
				return new Trade(stack, ess);
			}
		}
		else
		{
			return new Trade(money, ess);
		}
	}

	private void showError(final IEssentials ess, final CommandSource sender, final Throwable exception, final String signName)
	{
		ess.showError(sender, exception, "\\ sign: " + signName);
	}


	static class EventSign implements ISign
	{
		private final transient SignChangeEvent event;
		private final transient Block block;
		private final transient Sign sign;

		EventSign(final SignChangeEvent event)
		{
			this.event = event;
			this.block = event.getBlock();
			this.sign = (Sign)block.getState();
		}

		@Override
		public final String getLine(final int index)
		{
			StringBuilder builder = new StringBuilder();
			for (char c : event.getLine(index).toCharArray())
			{
				if (c < 0xF700 || c > 0xF747)
				{
					builder.append(c);
				}
			}
			return builder.toString();
			//return event.getLine(index); // Above code can be removed and replaced with this line when https://github.com/Bukkit/Bukkit/pull/982 is merged.
		}

		@Override
		public final void setLine(final int index, final String text)
		{
			event.setLine(index, text);
			sign.setLine(index, text);
			updateSign();
		}

		@Override
		public Block getBlock()
		{
			return block;
		}

		@Override
		public void updateSign()
		{
			sign.update();
		}
	}


	static class BlockSign implements ISign
	{
		private final transient Sign sign;
		private final transient Block block;

		BlockSign(final Block block)
		{
			this.block = block;
			this.sign = (Sign)block.getState();
		}

		@Override
		public final String getLine(final int index)
		{
			StringBuilder builder = new StringBuilder();
			for (char c : sign.getLine(index).toCharArray())
			{
				if (c < 0xF700 || c > 0xF747)
				{
					builder.append(c);
				}
			}
			return builder.toString();
			//return event.getLine(index); // Above code can be removed and replaced with this line when https://github.com/Bukkit/Bukkit/pull/982 is merged.
		}
		
		@Override
		public final void setLine(final int index, final String text)
		{
			sign.setLine(index, text);
		}

		@Override
		public final Block getBlock()
		{
			return block;
		}

		@Override
		public final void updateSign()
		{
			sign.update();
		}
	}


	public interface ISign
	{
		public String getLine(final int index);

		public void setLine(final int index, final String text);

		public Block getBlock();

		public void updateSign();
	}
}
