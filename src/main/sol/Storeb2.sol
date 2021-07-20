// SPDX-License-Identifier: Apache-2.0
pragma solidity ^0.6.0;

interface Storec2 {
    function setc(uint x) external;

    function getc() external view returns (uint);
}

contract Storeb2 {
    uint private storedValue;
    Storec2 anotherStorage;

    constructor (uint initVal, address _addrc) public {
        storedValue = initVal;
        anotherStorage = Storec2(_addrc);
    }

    function getc() public view returns (uint) {
        return anotherStorage.getc();
    }

    function getb() public view returns (uint) {
        return storedValue;
    }

    function setc(uint x) public {
        return anotherStorage.setc(x);
    }

    function setb(uint x) public {
        uint mc = anotherStorage.getc();
        storedValue = x * mc;
    }
}
