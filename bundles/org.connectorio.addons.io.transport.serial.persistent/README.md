# Persistent serial port provider for openHAB

This addon is a proof of concept work which is an attempt to solve troubles which comes from Linux serial port handling.
Currently, Linux is not able to provide consistent serial port identifiers across restarts.
This can lead to situation when `/dev/ttyUSB` can point to a device which was earlier `ttyUSB1`. 

Common advice in such scenarios is to use udev rules.
While it solves the problem, it forces user to dive into technical details on how to identify an USB device.
To simplify a basic user life this addon does what udev rules do in very limited scope which should satisfy most of the cases.

## How and when it works

This serial port provider calculates a hash of an USB device.
The hash includes several aspects:

- *Vendor id*
- *Product id*
- Product
- Manufacturer
- Interface Description
- Interface Number
- **Serial Number**

First two elements are mandatory for each USB device.
In theory, it should be unique between different products 
As long as you use different USB devices this provider should be able to handle them.

With above information your persistent serial port will be bound to a specific hardware.
The port or port identifier does not matter.
In fact this addon does specify initial port assignment, but it is there only for first setup.
The port lookup will be done later based on above hash information.

### When it doesn't work

The more information about USB device is available the more likely it is to stay unique.
However, it quite commonly happens that USB interfaces and adapters rely on fairly popular chips (electronics).
It might happen that you have a device which does something completely different from other, but still identify as the same.
This happens when a `SerialNumber` is not set.
It also indicates that manufacturer of that device did not take care of its identification.

If you face such situation then there is still a way get it working based on USB port numbers.
