package org.connectorio.addons.binding.opcua.internal;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.util.ConversionUtil.toList;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.SignedIdentityToken;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.UaStackClient;
import org.eclipse.milo.opcua.stack.client.UaStackClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.NamespaceTable;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseResultMask;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.BrowseResult;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  private static Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) throws Exception {
    OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();

    List<EndpointDescription> descriptions = DiscoveryClient.getEndpoints(
        "opc.tcp://localhost:4840").get();

    for (EndpointDescription description : descriptions) {
      System.out.println(description.getEndpointUrl() + " " + Arrays.toString(description.getUserIdentityTokens()));
    }

    OpcUaClientConfig config = OpcUaClientConfig.builder()
        .setApplicationName(LocalizedText.english("eclipse milo opc-ua client of the apache PLC4X:PLC4J project"))
        .setApplicationUri("urn:eclipse:milo:plc4x:client")
        .setEndpoint(descriptions.get(0))
//        .setIdentityProvider(new IdentityProvider() {
//          @Override
//          public SignedIdentityToken getIdentityToken(EndpointDescription endpointDescription, ByteString byteString) throws Exception {
//            return null;
//          }
//        })
        .setRequestTimeout(UInteger.valueOf(100))
        .build();

    OpcUaClient client = OpcUaClient.create(config);
    UaClient uaClient = client.connect().get();

    // start browsing at root folder
    //browseNode("", client, Identifiers.RootFolder);
    browseNode2("", client, Identifiers.ObjectsFolder);
    
  }


  private static void browseNode(String indent, OpcUaClient client, NodeId browseRoot) {
    BrowseDescription browse = new BrowseDescription(
        browseRoot,
        BrowseDirection.Forward,
        Identifiers.References,
        true,
        uint(NodeClass.Object.getValue() | NodeClass.Variable.getValue() | NodeClass.View.getValue()),
        uint(BrowseResultMask.All.getValue())
    );

    try {
      BrowseResult browseResult = client.browse(browse).get();

      List<ReferenceDescription> references = toList(browseResult.getReferences());

      for (ReferenceDescription rd : references) {
        System.out.println(indent + " Node=" + rd.getBrowseName().getName() + " " + rd.getDisplayName().getText());

        // recursively browse to children
        rd.getNodeId().toNodeId(client.getNamespaceTable())
            .ifPresent(nodeId -> browseNode(indent + "  ", client, nodeId));
      }
    } catch (InterruptedException | ExecutionException e) {
      logger.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
    }
  }

  private static void browseNode2(String indent, OpcUaClient client, NodeId browseRoot) {
      try {
          List<? extends UaNode> nodes = client.getAddressSpace().browseNodes(browseRoot);

          for (UaNode node : nodes) {
              System.out.println(indent + " Node=" + node.getBrowseName().getName() + " " + node.getDisplayName().getText() + " " + node.getNodeId());

              // recursively browse to children
              browseNode2(indent + "  ", client, node.getNodeId());
          }
      } catch (UaException e) {
          logger.error("Browsing nodeId={} failed: {}", browseRoot, e.getMessage(), e);
      }
  }
}
