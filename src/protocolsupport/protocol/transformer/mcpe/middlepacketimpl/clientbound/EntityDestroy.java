package protocolsupport.protocol.transformer.mcpe.middlepacketimpl.clientbound;

import java.io.IOException;
import java.util.UUID;

import gnu.trove.map.hash.TIntObjectHashMap;

import protocolsupport.api.ProtocolVersion;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.ClientboundPEPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound.RemoveEntityPacket;
import protocolsupport.protocol.transformer.mcpe.packet.mcpe.clientbound.RemovePlayerPacket;
import protocolsupport.protocol.transformer.mcpe.utils.PEWatchedPlayer;
import protocolsupport.protocol.transformer.middlepacket.clientbound.play.MiddleEntityDestroy;
import protocolsupport.protocol.typeremapper.watchedentity.remapper.SpecificType;
import protocolsupport.protocol.typeremapper.watchedentity.types.WatchedEntity;
import protocolsupport.utils.recyclable.RecyclableArrayList;
import protocolsupport.utils.recyclable.RecyclableCollection;

public class EntityDestroy extends MiddleEntityDestroy<RecyclableCollection<? extends ClientboundPEPacket>> {

	private int selfWatchedId;
	private final TIntObjectHashMap<UUID> players = new TIntObjectHashMap<>();

	@Override
	public void handle() {
		selfWatchedId = storage.getWatchedSelfPlayer().getId();
		players.clear();
		for (int id : entityIds) {
			WatchedEntity wentity = storage.getWatchedEntity(id);
			if (wentity != null && wentity.getType() == SpecificType.PLAYER) {
				players.put(id, ((PEWatchedPlayer) wentity).getUUID());
			}
		}
		storage.getPEStorage().removeItemsInfo(entityIds);
		super.handle();
	}

	@Override
	public RecyclableCollection<? extends ClientboundPEPacket> toData(ProtocolVersion version) throws IOException {
		RecyclableCollection<ClientboundPEPacket> datas = RecyclableArrayList.create();
		for (int id : entityIds) {
			if (selfWatchedId == id) {
				continue;
			}
			if (players.containsKey(id)) {
				datas.add(new RemovePlayerPacket(id, players.get(id)));
			} else {
				datas.add(new RemoveEntityPacket(id));
			}
		}
		return datas;
	}

}
