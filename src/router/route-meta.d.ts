import 'vue-router';

declare module 'vue-router' {
  interface RouteMeta {
    public?: boolean;
    title?: string;
    roles?: string[];
    permissions?: string[];
    skipAccessCheck?: boolean;
  }
}
