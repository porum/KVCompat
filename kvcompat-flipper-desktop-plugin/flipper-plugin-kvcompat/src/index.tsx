import {
  Select
} from 'antd';

import React, { useState } from 'react';
import { PluginClient, usePlugin, createState, useValue, DataInspector, Layout } from 'flipper-plugin';
import { clone } from 'lodash';

type ValueChangeEvent = {
  module: string;
  key: string;
  time: number;
  deleted: boolean;
  value?: any;
};

// key-value集合
type KVMap = Record<string, any>;

// 每个module的实体类，包含该module的kv数据，以及更改记录。
type ModuleEntry = {
  kvMap: KVMap;
  changesList: Array<ValueChangeEvent>;
};

export type PutValueParams = {
  module: string;
  key: string;
  value: any;
};

type DeleteKeyParams = {
  module: string;
  key: string;
};

type Events = {
  valueChange: ValueChangeEvent;
  addModule: any;
};
type Methods = {
  getAllModules: (params: {}) => Promise<Record<string, KVMap>>;
  putValue: (params: PutValueParams) => Promise<KVMap>;
  deleteKey: (params: DeleteKeyParams) => Promise<KVMap>;
};


// Read more: https://fbflipper.com/docs/tutorial/js-custom#creating-a-first-plugin
// API: https://fbflipper.com/docs/extending/flipper-plugin#pluginclient
export function plugin(client: PluginClient<Events, Methods>) {
  const selectedModule = createState<string | null>(null, { persist: 'selectedModule' });
  const setSelectedModule = (value: string) => selectedModule.set(value);
  const modules = createState<Record<string, ModuleEntry>>({}, { persist: 'modules' });

  function updateModule(update: { module: string; kvMap: any }) {
    if (selectedModule.get() == null) {
      selectedModule.set(update.module);
    }
    modules.update((draft) => {
      const entry = draft[update.module] || { changesList: [] };
      entry.kvMap = update.kvMap;
      draft[update.module] = entry;
    });
  }

  async function putValue(params: PutValueParams) {
    const results = await client.send('putValue', params);
    updateModule({
      module: params.module,
      kvMap: results,
    });
  }

  async function deleteKey(params: DeleteKeyParams) {
    const results = await client.send('deleteKey', params);
    updateModule({
      module: params.module,
      kvMap: results,
    });
  }

  client.onMessage('valueChange', (change) =>
    modules.update((draft) => {
      const entry = draft[change.module];
      if (entry == null) {
        return;
      }
      if (change.deleted) {
        delete entry.kvMap[change.key];
      } else {
        entry.kvMap[change.key] = change.value;
      }
      entry.changesList.unshift(change);
      draft[change.module] = entry;
    }),
  );

  client.onMessage('addModule', (event) => {
    Object.entries(event).forEach(([module, kvMap]) =>
      updateModule({
        module: module,
        kvMap: kvMap,
      })
    )
  });

  client.onConnect(async () => {
    const results = await client.send('getAllModules', {});
    Object.entries(results).forEach(([module, kvMap]) =>
      updateModule({
        module: module,
        kvMap: kvMap,
      })
    );
  })

  return {
    selectedModule,
    modules,
    setSelectedModule,
    putValue,
    deleteKey
  };
}


// Read more: https://fbflipper.com/docs/tutorial/js-custom#building-a-user-interface-for-the-plugin
// API: https://fbflipper.com/docs/extending/flipper-plugin#react-hooks
export function Component() {
  const instance = usePlugin(plugin);
  const selectedModule = useValue(instance.selectedModule);
  const modules = useValue(instance.modules);

  if (selectedModule == null) {
    return null;
  }
  const entry = modules[selectedModule];
  if (entry == null) {
    return null;
  }

  // 如果使用react18，使用DataInspector可能会报这个错： https://github.com/facebook/flipper/issues/3996
  return (
    <Layout.Container pad>
      <Layout.Container>
        <Select value={selectedModule} onChange={instance.setSelectedModule}>
          {Object.keys(modules)
            .sort((a, b) => (a.toLowerCase() > b.toLowerCase() ? 1 : -1))
            .map((name) => (
              <Select.Option key={name} value={name}>
                {name}
              </Select.Option>
            ))}
        </Select>
      </Layout.Container>
      <DataInspector
        data={entry.kvMap}
        setValue={async (path: Array<string>, value: any) => {
          if (entry == null) {
            return;
          }
          const values = entry.kvMap;
          let newValue = value;
          if (path.length === 2 && values) {
            newValue = clone(values[path[0]]);
            newValue[path[1]] = value;
          }
          await instance.putValue({
            module: selectedModule,
            key: path[0],
            value: newValue,
          });
        }}
        onDelete={async (path: Array<string>) =>
          await instance.deleteKey({
            module: selectedModule,
            key: path[0],
          })
        }
      />
    </Layout.Container>
  );
}
