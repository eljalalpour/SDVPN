#!/bin/bash

i=0
while read name details
do
	if [ $i -gt 0 ]; then
		echo "[$i]: $name : $details"
		capture_interfaces="$capture_interfaces $name"
	else
		echo "[ ]: $name : $details"
	fi
	let i=$i+1
done < <(ifconfig -s)

select capture_interface in $capture_interfaces
do
	if [ ! -z "$capture_interface" ]; then
		echo "You select $capture_interface"
		break
	else
		echo "$REPLAY is not valid"
	fi
done

if [ ! -d "captures" ]; then
	mkdir captures
fi

capture_file=captures/$(date +"%F_%H:%m:%S")-$capture_interface.pcap

touch "$capture_file"
sudo chown root:root "$capture_file"
echo "Capturing output file is ready for write."

sudo tshark -i "$capture_interface" -F pcap -P -w "$capture_file"
echo "Capturing done."

sudo chown parham:parham "$capture_file"
echo "Capturing output file is ready for read."
