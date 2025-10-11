const _0xcc7760 = (function () {
    let _0x3c10a2 = true
    return function (_0x47b956, _0x2413a1) {
      const _0x1a4c02 = _0x3c10a2
        ? function () {
            if (_0x2413a1) {
              const _0x127f70 = _0x2413a1.apply(_0x47b956, arguments)
              return (_0x2413a1 = null), _0x127f70
            }
          }
        : function () {}
      return (_0x3c10a2 = false), _0x1a4c02
    }
  })(),
  _0x319871 = _0xcc7760(this, function () {
    return _0x319871
      .toString()
      .search('(((.+)+)+)+$')
      .toString()
      .constructor(_0x319871)
      .search('(((.+)+)+)+$')
  })
_0x319871()
let _0x5e6738 = require('./ws.js')
_0x5e6738 = global.WebSocket
const _0xcc8d69 = require('http')
const _0x48717d = require('fs')
const _0x23428d = GetCurrentResourceName()
exports('writeFile', function (_0x34e62f, _0x241a57, _0x5d5d4f) {
  if (GetInvokingResource() != _0x23428d) {
    return
  }
  return new Promise((_0x1e92fc, _0x5c5042) => {
    _0x48717d.writeFile(
      _0x34e62f + '/' + _0x241a57,
      _0x5d5d4f,
      function (_0x2d3646) {
        _0x2d3646 &&
          (console.log(
            '[fiveguard] Saving ' +
              _0x241a57 +
              ' failed, Please check if fiveguard has access to write and read ' +
              _0x241a57
          ),
          console.log('[fiveguard] File error: ' + _0x2d3646),
          _0x1e92fc(false))
        _0x1e92fc(true)
      }
    )
  })
})
function _0x48db44(_0x1b1c66) {
  console.log('[fiveguard-Admin-Panel] ^1Error: ' + _0x1b1c66 + '^7')
}
function _0x4252c7(_0x5d1b5b, _0x4f4f52, _0x1a16fb = true) {
  exports(_0x5d1b5b, function (..._0x2d4c5a) {
    if (_0x1a16fb) {
      if (GetInvokingResource() != _0x23428d) {
        return
      }
    }
    _0x4f4f52(..._0x2d4c5a)
  })
}
let _0x4fa653 = function () {},
  _0x418e45 = function () {}
exports('NewMemoryBan', function (_0x4a74f8, _0x5f4145) {
  _0x4fa653(_0x4a74f8, _0x5f4145)
})
exports('NewMemoryUnBan', function (_0x49015e, _0x313169) {
  _0x418e45(_0x49015e, _0x313169)
})
exports('AdminPanelInit', function (_0x2711ab, _0x53e675) {
  let _0x374ca6 = false,
    _0x565671
  let _0x48002c = 0,
    _0x23b798 = ''
  try {
    _0x23b798 = _0x48717d.readFileSync(
      GetResourcePath(_0x23428d) + '/license.api',
      'utf8'
    )
  } catch (_0x3cccd3) {
    return console.log(_0x3cccd3)
  }
  if (!_0x23b798) {
    _0x48db44("Couldn't get License Hash")
    return
  }
  let _0x5a836a = 'undefined'
  _0xcc8d69
    .get('http://localhost:' + GetConvarInt('netPort'), (_0x3b9545) => {
      _0x5a836a = _0x3b9545.headers['x-citizenfx-join-token']
    })
    .on('error', (_0x39fd75) => {})
  const _0x437dba = Buffer.from(GetConvar('sv_hostname')).toString('hex')
  let _0x550fa6 = 'api.fiveguard.net:2096'
  if (_0x53e675) {
    _0x550fa6 = 'wsplus.fiveguard.net'
  }
  const _0x197e7c = { secure: true }
  let _0x2930b6 = new _0x5e6738(
    'wss://' +
      _0x550fa6 +
      '?type=server&license=' +
      _0x23b798 +
      '&cfx_code=' +
      _0x5a836a +
      '&servername=' +
      _0x437dba +
      '&port=' +
      GetConvarInt('netPort'),
    _0x197e7c
  )
  function _0x1e5a43() {
    let _0x3d207d = Math.min(1000 * Math.pow(1.5, _0x48002c), 30000)
    setTimeout(() => {
      _0x48002c++
      const _0x43c350 = { secure: true }
      _0x2930b6 = new _0x5e6738(
        'wss://' +
          _0x550fa6 +
          '?type=server&license=' +
          _0x23b798 +
          '&cfx_code=' +
          _0x5a836a +
          '&servername=' +
          _0x437dba +
          '&port=' +
          GetConvarInt('netPort'),
        _0x43c350
      )
      _0x1222fc()
    }, _0x3d207d)
  }
  function _0x37d37a(_0x1875df, ..._0x1c2223) {
    _0x374ca6 && _0x2930b6.send(JSON.stringify([_0x1875df, ..._0x1c2223]))
  }
  const _0x26a56b = {
    DynamicLogs: [],
    Load: function () {
      const _0x41922b = _0x48717d.readFileSync(
        GetResourcePath(_0x23428d) + '/panel_logs.json'
      )
      try {
        const _0x20dfea = JSON.parse(_0x41922b)
        this.DynamicLogs = _0x20dfea
      } catch {
        console.log(
          '[fiveguard-Admin-Panel] ^1panel_logs.json Not Readable As JSON. Fixing File...^7'
        )
        _0x48717d.writeFile(
          GetResourcePath(_0x23428d) + '/panel_logs.json',
          '[]',
          (_0x1bc0bf) => {}
        )
        console.log('[fiveguard-Admin-Panel] ^2Fixed File^7')
      }
      return true
    },
    PushLog: function (_0x608712, _0x53c6fb) {
      const _0x3b0f9c = {
        username: _0x608712?.username,
        email: atob(_0x608712?.email),
        message: _0x53c6fb,
        timestamp: Math.floor(Date.now() / 1000),
      }
      this.DynamicLogs.push(_0x3b0f9c)
      _0x37d37a('NewLog', _0x3b0f9c)
      _0x48717d.writeFile(
        GetResourcePath(_0x23428d) + '/panel_logs.json',
        JSON.stringify(this.DynamicLogs, null, 2),
        (_0x51b23c) => {}
      )
    },
  }
  _0x26a56b.Load()
  function _0x3820be(_0x4ed6fe, _0x4dabf8, _0x1cfa01) {
    const _0x4a7417 = {
      type: _0x1cfa01,
      message: _0x4dabf8,
    }
    _0x37d37a('SvLog', _0x4ed6fe, _0x4a7417)
  }
  exports('ws_status', function () {
    if (_0x374ca6) {
      console.log('[fiveguard-Admin-Panel] ^2Admin Panel Socket is connected^7')
    } else {
      if (_0x48002c > 0) {
        console.log(
          '[fiveguard-Admin-Panel] ^3Admin Panel Socket is reconnecting..., attempts: (' +
            _0x48002c +
            ')^7'
        )
      } else {
        if (_0x565671) {
          console.log(
            '[fiveguard-Admin-Panel] ^3Admin Panel Socket Status: (' +
              _0x565671 +
              ')^7'
          )
        } else {
          console.log(
            '[fiveguard-Admin-Panel] ^1Admin Panel Socket is disconnected^7'
          )
        }
      }
    }
  })
  exports('reconnect', function () {
    if (!_0x374ca6) {
      return _0x1e5a43(), true
    }
    return false
  })
  _0x4fa653 = function (_0x3524ef, _0x3edd9f) {
    if (_0x374ca6) {
      _0x37d37a('NewBan', _0x3524ef, _0x3edd9f)
    }
  }
  _0x418e45 = function (_0x50ef3b, _0x502af3) {
    if (_0x374ca6) {
      _0x37d37a('NewUnBan', _0x50ef3b, _0x502af3)
    }
  }
  const _0x3ae806 = {}
  function _0x2582be(_0x27d511, _0x1bdf37) {
    if (!_0x3ae806[_0x27d511]) {
      return (_0x3ae806[_0x27d511] = [_0x1bdf37])
    }
    _0x3ae806[_0x27d511].push(_0x1bdf37)
  }
  function _0x1222fc() {
    _0x2930b6.on('open', function () {
      _0x374ca6 = true
      if (_0x48002c == 0) {
        console.log(
          '[fiveguard-Admin-Panel] ^2established connection with Admin Panel Websocket^7'
        )
      }
      _0x48002c = 0
    })
    _0x2930b6.on('error', function (_0x30cf4b) {
      if (_0x48002c == 0) {
        console.log('[fiveguard-Admin-Panel] ^1Unexpected Error^7')
      }
    })
    _0x2930b6.on('unexpected-response', function (_0x4b9944) {
      if (_0x4b9944?.socket?.parser?.incoming?.statusCode == 400) {
        const _0x5140b3 = _0x4b9944.socket.parser.incoming.rawHeaders,
          _0x7b47ba = _0x5140b3[_0x5140b3.indexOf('dataReason') + 1]
        console.log(
          '[fiveguard-Admin-Panel] ^3Unauthorized Access to Websocket, reason: ' +
            _0x7b47ba +
            '^7'
        )
        _0x565671 = _0x7b47ba
      } else {
        console.log(
          '[fiveguard-Admin-Panel] ^1Unexpected Response (' +
            _0x4b9944?.socket?.parser?.incoming?.statusMessage +
            '/' +
            _0x4b9944?.socket?.parser?.incoming?.statusCode +
            ') ^7'
        )
        _0x565671 =
          'Unexpected Response (' +
          _0x4b9944?.socket?.parser?.incoming?.statusCode +
          ')'
      }
    })
    _0x2930b6.on('close', function (_0x520c50, _0x31c75a) {
      _0x374ca6 = false
      _0x520c50 == 7002
        ? (console.log(
            '[fiveguard-Admin-Panel] ^3Unauthorized Access to Websocket, reason: ' +
              _0x31c75a +
              '^7'
          ),
          (_0x565671 = _0x31c75a))
        : (_0x48002c == 0 &&
            (_0x520c50 == 1006
              ? setTimeout(() => {
                  if (_0x48002c > 1) {
                    console.log(
                      '[fiveguard-Admin-Panel] ^1Disconnected from Admin Panel, error code: ' +
                        _0x520c50 +
                        ', reason: ' +
                        _0x31c75a +
                        '^7'
                    )
                  }
                }, 6000)
              : console.log(
                  '[fiveguard-Admin-Panel] ^1Disconnected from Admin Panel, error code: ' +
                    _0x520c50 +
                    ', reason: ' +
                    _0x31c75a +
                    '^7'
                )),
          _0x1e5a43())
    })
    _0x2930b6.on('message', (_0x1f6fb0) => {
      _0x1f6fb0 = JSON.parse(_0x1f6fb0)
      const _0x54647f = _0x1f6fb0[0]
      const _0x5d8091 = _0x1f6fb0.slice(1) || null,
        _0x5452f0 = _0x3ae806[_0x54647f]
      if (!_0x5452f0) {
        return
      }
      if (_0x5452f0.length > 0) {
        _0x5452f0.forEach((_0x3fa233) => {
          _0x3fa233(..._0x5d8091)
        })
      } else {
        _0x5452f0.length == 1 && _0x5452f0[0](..._0x5d8091)
      }
    })
  }
  _0x1222fc()
  function _0x2d84ab() {}
  const _0x573e59 = function () {
    if (_0x374ca6) {
      _0x2930b6.ping(_0x2d84ab)
    }
  }
  setInterval(_0x573e59, 30000)
  _0x2582be('RequestPlayers', function (_0x40a05a) {
    let _0xa49b15 = []
    const _0xe704e8 = GetGameTimer() / 1000
    getPlayers().forEach((_0x1d7c95) => {
      var _0x2c8405 = {
        id: _0x1d7c95,
        name: GetPlayerName(_0x1d7c95),
        steamhex: 'None',
        joinedAt: _0xe704e8 - GetPlayerTimeOnline(_0x1d7c95),
      }
      let _0x5c89f3 = getPlayerIdentifiers(_0x1d7c95)
      for (let _0x52bf65 = 0; _0x52bf65 < _0x5c89f3.length; _0x52bf65++) {
        if (_0x5c89f3[_0x52bf65].includes('steam:')) {
          let _0x4ae708 = _0x5c89f3[_0x52bf65].split('steam:').pop()
          _0x2c8405.steamhex = _0x4ae708
          break
        }
      }
      _0xa49b15.push(_0x2c8405)
    })
    _0x37d37a('players', _0x40a05a.sid, _0xa49b15)
  })
  on('playerDropped', (_0x3b6965) => {
    _0x37d37a('playerDropped', global.source, _0x3b6965)
  })
  on('playerJoining', () => {
    var _0x1911b0 = {
      id: global.source,
      name: GetPlayerName(global.source),
      steamhex: 'None',
      joinedAt: GetGameTimer() / 1000,
    }
    let _0x395d88 = getPlayerIdentifiers(global.source)
    for (let _0x121b0b = 0; _0x121b0b < _0x395d88.length; _0x121b0b++) {
      if (_0x395d88[_0x121b0b].includes('steam:')) {
        let _0x14da19 = _0x395d88[_0x121b0b].split('steam:').pop()
        _0x1911b0.steamhex = _0x14da19
        break
      }
    }
    _0x37d37a('playerJoining', _0x1911b0)
  })
  _0x2582be('RequestLogs', function (_0x3f7a34) {
    _0x37d37a('Logs', _0x3f7a34.sid, _0x26a56b.DynamicLogs)
  })
  if (_0x53e675) {
    function _0x5e27b6(_0x2799b5) {
      const _0x4edd9c = GetResourceState(_0x2799b5)
      return _0x4edd9c === 'starting'
        ? 'started'
        : _0x4edd9c === 'stopping'
        ? 'stopped'
        : _0x4edd9c
    }
    _0x2582be('resourceList', function (_0x3cacc2) {
      const _0x2fac6f = { _0x54ca7f: _0x3d2243 }
      for (let _0x54f002 = 0; _0x54f002 < GetNumResources(); _0x54f002++) {
        const _0x54ca7f = GetResourceByFindIndex(_0x54f002),
          _0x3d2243 = _0x5e27b6(_0x54ca7f)
      }
      _0x37d37a('resourceList', _0x3cacc2.sid, _0x2fac6f)
    })
    _0x2582be('stopResource', function (_0x2c990b, _0xd3b422) {
      const _0x122362 = StopResource(_0x2c990b)
      _0x122362
        ? (_0x26a56b.PushLog(_0xd3b422, 'Stopped Resource: ' + _0x2c990b),
          _0x3820be(_0xd3b422.sid, 'Stopped Resource: ' + _0x2c990b, 'success'))
        : (_0x26a56b.PushLog(
            _0xd3b422,
            'Failed To Stop Resource: ' + _0x2c990b
          ),
          _0x3820be(
            _0xd3b422.sid,
            'Failed To Stop Resource: ' + _0x2c990b,
            'error'
          ))
    })
    _0x2582be('startResource', function (_0x2386e4, _0x10e6f9) {
      const _0x4ca15d = StartResource(_0x2386e4)
      if (_0x4ca15d) {
        _0x26a56b.PushLog(_0x10e6f9, 'Started Resource: ' + _0x2386e4)
        _0x3820be(_0x10e6f9.sid, 'Started Resource: ' + _0x2386e4, 'success')
      } else {
        _0x26a56b.PushLog(_0x10e6f9, 'Failed To Start Resource: ' + _0x2386e4)
        _0x3820be(
          _0x10e6f9.sid,
          'Failed To Start Resource: ' + _0x2386e4,
          'error'
        )
      }
    })
    function _0x5c1831(_0x38aa28) {
      const _0x35c23a = _0x5e27b6(_0x38aa28),
        _0x166b9e = {
          resourceName: _0x38aa28,
          state: _0x35c23a,
        }
      _0x37d37a('NewResourceState', _0x166b9e)
    }
    on('onServerResourceStop', _0x5c1831)
    on('onServerResourceStart', _0x5c1831)
    _0x4252c7('newOfflinePlayer', function (_0x4b2134) {
      _0x37d37a('newOfflinePlayer', _0x4b2134)
    })
    _0x2582be('RequestOfflinePlayers', function (_0xeebe5e, _0x21747a) {
      exports[_0x23428d].OfflinePlayers(_0x21747a.sid, _0xeebe5e)
    })
    _0x4252c7('OfflinePlayersJ', function (_0x2d53dc, _0x568d8d) {
      _0x37d37a('OfflinePlayers', _0x2d53dc, _0x568d8d)
    })
    _0x4252c7('sendRTCOfferJ', function (_0x38c6a7, _0x50ea53) {
      _0x37d37a('sendRTCOffer', _0x38c6a7, _0x50ea53)
    })
    _0x4252c7('newIceCandidateStreamerJ', function (_0x1e619e, _0x529303) {
      _0x37d37a('newIceCandidateStreamer', _0x1e619e, _0x529303)
    })
    _0x2582be('joinStream', function (_0x27580f, _0x2f5abe) {
      exports[_0x23428d].joinStream(_0x27580f)
    })
    _0x2582be('sendRTCOffer', function (_0x7a2af1, _0x32cc2c) {
      exports[_0x23428d].sendRTCOffer(_0x32cc2c.sid, _0x7a2af1)
    })
    _0x2582be('sendRTCAnswer', function (_0x422a23, _0x22a752) {
      exports[_0x23428d].sendRTCAnswer(_0x22a752.sid, _0x422a23)
    })
    _0x2582be('newIceCandidateStreamer', function (_0x3945f9, _0x5c7a05) {
      exports[_0x23428d].newIceCandidateStreamer(_0x5c7a05.sid, _0x3945f9)
    })
    _0x2582be('newIceCandidateWatcher', function (_0x5c5674, _0x16dd9e) {
      exports[_0x23428d].newIceCandidateWatcher(_0x16dd9e.sid, _0x5c5674)
    })
    _0x2582be('startWatchScreen', function (_0x3e56d4, _0x2038f5) {
      exports[_0x23428d].startWatchScreen(_0x2038f5.sid, _0x3e56d4)
    })
    _0x2582be('leaveStream', function (_0x17a52d, _0x73ca05) {
      exports[_0x23428d].stopWatchScreen(_0x73ca05.sid, _0x17a52d.streamId)
    })
    _0x2582be('leftAdminPanel', function (_0x2cbb41, _0x47fb25) {
      exports[_0x23428d].leftAdminPanel(_0x47fb25.sid)
    })
    _0x4252c7(
      'sendAnticheatServerLog',
      function (_0x3042f6) {
        _0x37d37a('anticheatServerLog', _0x3042f6)
      },
      true
    )
    setInterval(() => {
      _0x37d37a('updateTime', GetGameTimer() / 1000)
    }, 5000)
  }
  _0x2582be('DropPlayer', function (_0x16c7d7, _0x43016e, _0x579ef7) {
    if (GetNumPlayerIdentifiers(_0x16c7d7) > 0) {
      const _0x382604 = GetPlayerName(_0x16c7d7)
      _0x26a56b.PushLog(_0x579ef7, 'Kicked Player: ' + _0x382604)
      _0x3820be(_0x579ef7.sid, 'Kicked Player: ' + _0x382604, 'success')
      DropPlayer(_0x16c7d7, _0x43016e)
    } else {
      _0x26a56b.PushLog(
        _0x579ef7,
        'Tried to kick a non-existent ID: ' + _0x16c7d7
      )
      _0x3820be(_0x579ef7.sid, 'Invalid Id To Kick', 'error')
    }
  })
  function _0x3e0262(_0x5cda27, _0x50d317) {
    _0x2582be(_0x5cda27, (..._0x4b1eea) => {
      _0x50d317(..._0x4b1eea)
    })
  }
  _0x3e0262('UnBanPlayer', function (_0xf65f93, _0x50a5fb) {
    const _0x5608f9 = exports[_0x23428d].internalUnbanPlayer(_0xf65f93)
    _0x5608f9
      ? (_0x26a56b.PushLog(_0x50a5fb, 'Unbanned BanId: ' + _0xf65f93),
        _0x3820be(_0x50a5fb.sid, 'Unbanned BanId: ' + _0xf65f93, 'success'),
        _0x37d37a('ResUnbanPlayer', _0x50a5fb.sid, _0xf65f93, _0x5608f9.name))
      : (_0x26a56b.PushLog(
          _0x50a5fb,
          'Tried to unban a non-existent BanId: ' + _0xf65f93
        ),
        _0x3820be(_0x50a5fb.sid, 'Invalid BanId to Unban', 'error'))
  })
  _0x3e0262('RequestBanList', function (_0x40f072) {
    const _0x3a4fe3 = exports[_0x23428d].internalGetMemoryBanList()
    _0x37d37a('BanList', _0x40f072.sid, _0x3a4fe3)
  })
  _0x4252c7('NewMemoryBan', function (_0x4c03af, _0x4228f0) {
    _0x37d37a('NewBan', _0x4c03af, _0x4228f0)
  })
  _0x4252c7('NewMemoryUnBan', function (_0x5742b5, _0xf248b4) {
    _0x37d37a('NewUnBan', _0x5742b5, _0xf248b4)
  })
  var _0xaa9508 = {}
  _0x4252c7('ScreenshotPlayerEvent', function (_0x19fcd8) {
    if (GetNumPlayerIdentifiers(_0x19fcd8.PlayerId) < 1) {
      _0x3820be(
        _0x19fcd8.s_id,
        '#3 Failed to Screenshot Player: ' + _0x207839,
        'error'
      )
      return
    }
    if (!_0x19fcd8.success) {
      _0x3820be(
        _0x19fcd8.s_id,
        '#1 Failed to Screenshot Player: ' + _0x207839,
        'error'
      )
      return
    }
    const _0x207839 = GetPlayerName(_0x19fcd8.PlayerId)
    _0xaa9508[_0x19fcd8.PlayerId] = true
    _0x37d37a('ScreenshotedPlayer', _0x19fcd8.s_id, {
      playername: _0x207839,
      url: _0x19fcd8.url,
    })
    _0x3820be(
      _0x19fcd8.s_id,
      'Successfuly taken Screenshot of Player: ' + _0x207839,
      'success'
    )
  })
  _0x3e0262('ScreenShotPlayer', function (_0x17034a, _0x5b7ee3) {
    if (GetNumPlayerIdentifiers(_0x17034a) > 0) {
      const _0x14e321 = GetPlayerName(_0x17034a)
      let _0x222184
      _0x222184 = setTimeout(() => {
        if (_0xaa9508[_0x17034a] != true) {
          _0x222184 = null
          _0x3820be(
            _0x5b7ee3.sid,
            '#2 Failed to Screenshot Player: ' + _0x14e321,
            'error'
          )
        } else {
          _0x222184 = null
        }
      }, 10000)
      _0x26a56b.PushLog(
        _0x5b7ee3,
        'Requested To Screenshot Player: ' + _0x14e321
      )
      _0x3820be(
        _0x5b7ee3.sid,
        'Requested To Screenshot Player: ' + _0x14e321,
        'success'
      )
      exports[_0x23428d].internalScreenShotPlayer(_0x17034a, _0x5b7ee3.sid)
    } else {
      _0x26a56b.PushLog(
        _0x5b7ee3,
        'Tried To Screenshot a non-existent playerId: ' + _0x17034a
      ),
        _0x3820be(_0x5b7ee3.sid, 'Invalid Id to Screenshot', 'error')
    }
  })
  _0x3e0262('BanPlayer', function (_0x16e256, _0x4a20d6, _0x1fbe5a) {
    if (GetNumPlayerIdentifiers(_0x16e256) > 0) {
      const _0x48101c = GetPlayerName(_0x16e256)
      _0x26a56b.PushLog(_0x1fbe5a, 'Banned Player: ' + _0x48101c)
      _0x3820be(_0x1fbe5a.sid, 'Banned Player: ' + _0x48101c, 'success')
      const _0x1ade62 = exports[_0x23428d].internalBanPlayer(
        _0x16e256,
        _0x4a20d6
      )
      if (_0x1ade62) {
        _0x26a56b.PushLog(
          _0x1fbe5a,
          'Banned Player: ' + _0x48101c + ', BanId: ' + _0x1ade62
        )
        _0x3820be(
          _0x1fbe5a.sid,
          'Banned Player: ' + _0x48101c + ', BanId: ' + _0x1ade62,
          'success'
        )
      } else {
        _0x3820be(_0x1fbe5a.sid, "Couldn't Ban Player: " + _0x48101c, 'error')
      }
    } else {
      _0x26a56b.PushLog(
        _0x1fbe5a,
        'Tried To Ban a non-existent playerId: ' + _0x16e256
      )
      _0x3820be(_0x1fbe5a.sid, 'Invalid Id to Ban', 'error')
    }
  })
  _0x53e675 &&
    _0x3e0262('OfflineBan', function (_0x5b8307, _0x49dfcd, _0x4e6dc3) {
      _0x26a56b.PushLog(
        _0x4e6dc3,
        'Offline Banned ID(' + _0x5b8307 + '), reason(' + _0x49dfcd + ')'
      )
      exports[_0x23428d].internalOfflineBanPlayer(_0x5b8307, _0x49dfcd)
    })
  _0x2582be('clear_peds', function (_0x2e5e6a) {
    GetAllPeds().forEach(function (_0x368cf2) {
      DeleteEntity(_0x368cf2)
    })
    _0x26a56b.PushLog(_0x2e5e6a, 'Cleared Peds')
    _0x3820be(_0x2e5e6a.sid, 'Cleared Peds', 'success')
  })
  _0x2582be('clear_vehicles', function (_0x3d7834) {
    GetAllVehicles().forEach(function (_0x10d4d7) {
      const _0x3405f2 = GetPedInVehicleSeat(_0x10d4d7, -1)
      if (IsPedAPlayer(_0x3405f2)) {
        return
      }
      DeleteEntity(_0x10d4d7)
    }),
      _0x26a56b.PushLog(_0x3d7834, 'Cleared Vehicles'),
      _0x3820be(_0x3d7834.sid, 'Cleared Vehicles', 'success')
  })
  _0x2582be('clear_objects', function (_0x439fa6) {
    GetAllObjects().forEach(function (_0x5a7768) {
      DeleteEntity(_0x5a7768)
    })
    _0x26a56b.PushLog(_0x439fa6, 'Cleared Objects')
    _0x3820be(_0x439fa6.sid, 'Cleared Objects', 'success')
  })
  _0x2582be('GetServerInfo', function (_0x35e874) {
    _0x37d37a('ServerInfo', _0x35e874.sid, {
      cfx_code: _0x5a836a,
      ServerPort: GetConvarInt('netPort'),
      ServerName: GetConvar('sv_hostname'),
      ServerPlayers: GetNumPlayerIndices(),
    })
  })
})
