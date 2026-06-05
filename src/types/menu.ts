import type { Component } from 'vue';

export interface AppMenuItem {
  title: string;
  path: string;
  icon: Component;
  roles?: string[];
  permissions?: string[];
  children?: AppMenuItem[];
}
