const selfDefend = (function () {
    let initialized = true;
    return function (context, fn) {
        const wrapper = initialized
            ? function () {
                if (fn) {
                    const result = fn.apply(context, arguments);
                    fn = null;
                    return result;
                }
            }
            : function () {};
        initialized = false;
        return wrapper;
    };
})();

const antiDebug = selfDefend(this, function () {
    return antiDebug
        .toString()
        .search('(((.+)+)+)+$')
        .toString()
        .constructor(antiDebug)
        .search('(((.+)+)+)+$');
});
antiDebug();

const resourceName = GetCurrentResourceName();
const isServer = IsDuplicityVersion();
if (_0x3941d4) {
  if (isServer) {
    let resourceToken = '',
      isLoaded = false,
      pendingCalls = [];

    function queueCall(name, ...args) {
      pendingCalls.push({ name, args });
    }

    function callExportedFunction(name, ...args) {
      exports[resourceName][resourceName + name + '-' + resourceToken](...args);
    }

    function processPendingCalls() {
      for (let i = 0; i < pendingCalls.length; i++) {
        // If you have a wrapper for calling, use it here
        callExportedFunction(pendingCalls[i].name, ...pendingCalls[i].args);
      }
    }

    let intervalId;
    intervalId = setInterval(() => {
      if (!isLoaded) {
        TriggerEvent(resourceName + '-IsLoaded', function () {
          isLoaded = true;
          resourceToken = exports[resourceName][resourceName + 'T']();
          processPendingCalls();
          clearInterval(intervalId);
        });
      } else {
        clearInterval(intervalId);
      }
    }, 100);

    TriggerEvent(resourceName + '-IsLoaded', function () {
      isLoaded = true;
      resourceToken = exports[resourceName][resourceName + 'T']();
      clearInterval(intervalId);
    });

    let originalInvokeNative = Citizen.InvokeNative;

    CreateObject = function (...args) {
      if (isLoaded) {
        return exports[resourceName][resourceName + 'CreateObject-' + resourceToken](...args);
      } else {
        queueCall('CreateObject', ...args);
      }
    };

    CreateObjectNoOffset = function (...args) {
      if (isLoaded) {
        return exports[resourceName][resourceName + 'CreateObjectNoOffset-' + resourceToken](...args);
      } else {
        queueCall('CreateObjectNoOffset', ...args);
      }
    };

    CreateVehicle = function (...args) {
      if (isLoaded) {
        return exports[resourceName][resourceName + 'CreateVehicle-' + resourceToken](...args);
      } else {
        queueCall('CreateVehicle', ...args);
      }
    };

    CreatePed = function (...args) {
      if (isLoaded) {
        return exports[resourceName][resourceName + 'CreatePed-' + resourceToken](...args);
      } else {
        queueCall('CreatePed', ...args);
      }
    };

    CreateVehicleServerSetter = function (...args) {
      if (isLoaded) {
        return exports[resourceName][resourceName + 'CreateVehicleServerSetter-' + resourceToken](...args);
      } else {
        queueCall('CreateVehicleServerSetter', ...args);
      }
    };

    Citizen.InvokeNative = function (nativeHash, ...args) {
      switch (nativeHash) {
        case 796565596:
          return CreateObject(...args);
        case 1476658208:
          return CreateObjectNoOffset(...args);
        case 59371377:
          return CreatePed(...args);
        case 3715450378:
          return CreateVehicle(...args);
        case 1793400139:
          return CreateVehicleServerSetter(...args);
        default:
          return originalInvokeNative(nativeHash, ...args);
      }
    };
  }
  let _0x101589 = '',
    _0x267f96 = false,
    _0x463010 = []
  function _0x5aa32d(_0x5083a4, ..._0x4ff098) {
    let _0x3f8937 = [..._0x4ff098]
    const _0x4d0d33 = {
      name: _0x5083a4,
      args: _0x3f8937,
    }
    _0x463010.push(_0x4d0d33)
  }
  function _0x1dc865(_0x8e880a, ..._0x3b47f2) {
    exports[_0x4b44c2][_0x4b44c2 + (_0x8e880a + '-') + _0x101589](
      _0x3b4733(),
      ..._0x3b47f2
    )
  }
  function _0x181c65() {
    for (let _0x4a2fd6 = 0; _0x4a2fd6 < _0x463010.length; _0x4a2fd6++) {
      _0x2defba.QiPZe(
        _0x1dc865,
        _0x463010[_0x4a2fd6].name,
let clientToken = '';
let clientLoaded = false;
let clientPendingCalls = [];

        function queueClientCall(name, ...args) {
          clientPendingCalls.push({ name, args });
}

        function callClientExportedFunction(name, ...args) {
          exports[resourceName][resourceName + name + '-' + clientToken](generateClientId(), ...args);
        }

        function processClientPendingCalls() {
          for (let i = 0; i < clientPendingCalls.length; i++) {
            callClientExportedFunction(clientPendingCalls[i].name, ...clientPendingCalls[i].args);
          }
        }

        function seededRandom(seed) {
          let value = seed % 4294967296;
          return function () {
            value = (1664525 * value + 1013904223) % 4294967296;
            return value / 4294967296;
          };
        }

        let randomGenerator;
        function generateClientId() {
          const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
          let id = '';
          for (let i = 0; i < 8; i++) {
            const idx = Math.floor(randomGenerator() * chars.length);
            id += chars[idx];
          }
          return id;
        }

        let clientIntervalId;
        clientIntervalId = setInterval(() => {
          if (!clientLoaded) {
            TriggerEvent(resourceName + '-IsLoaded', function () {
              clientLoaded = true;
              const tokenData = exports[resourceName][resourceName + 'T']();
              clientToken = tokenData[0];
              randomGenerator = seededRandom(tokenData[1]);
              processClientPendingCalls();
              clearInterval(clientIntervalId);
            });
          } else {
            clearInterval(clientIntervalId);
          }
        }, 100);

        TriggerEvent(resourceName + '-IsLoaded', function () {
          clientLoaded = true;
          const tokenData = exports[resourceName][resourceName + 'T']();
          clientToken = tokenData[0];
          randomGenerator = seededRandom(tokenData[1]);
          clearInterval(clientIntervalId);
        });

        let originalClientInvokeNative = Citizen.InvokeNative;

        CreateObject = function (...args) {
          if (clientLoaded) {
            return exports[resourceName][resourceName + 'CreateObject-' + clientToken](generateClientId(), ...args);
          } else {
            queueClientCall('CreateObject', ...args);
          }
        };

        CreateObjectNoOffset = function (...args) {
          if (clientLoaded) {
            return exports[resourceName][resourceName + 'CreateObjectNoOffset-' + clientToken](generateClientId(), ...args);
          } else {
            queueClientCall('CreateObjectNoOffset', ...args);
          }
        };

        CreateVehicle = function (...args) {
          if (clientLoaded) {
            return exports[resourceName][resourceName + 'CreateVehicle-' + clientToken](generateClientId(), ...args);
          } else {
            queueClientCall('CreateVehicle', ...args);
          }
        };

        CreatePed = function (...args) {
          if (clientLoaded) {
            return exports[resourceName][resourceName + 'CreatePed-' + clientToken](generateClientId(), ...args);
          } else {
            queueClientCall('CreatePed', ...args);
          }
        };

        Citizen.InvokeNative = function (nativeHash, ...args) {
          switch (nativeHash) {
            case 5808896370743568000:
            case 796565596:
              return CreateObject(...args);
            case 11108492561942820000:
            case 1476658208:
              return CreateObjectNoOffset(...args);
            case 15321134921733597000:
            case 59371377:
              return CreatePed(...args);
            case 12625226732244324000:
            case 3715450378:
              return CreateVehicle(...args);
            default:
              return originalClientInvokeNative(nativeHash, ...args);
          }
        }
