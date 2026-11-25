import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  View, StyleSheet, Pressable, Alert, PanResponder, useWindowDimensions,
} from 'react-native';
import Svg, { Circle, G, Path, Rect, Text as SvgText } from 'react-native-svg';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Crosshair, Minus, Plus } from 'lucide-react-native';
import { Screen } from '@/components/ui/Screen';
import { TopBar } from '@/components/ui/TopBar';
import { AppText } from '@/components/ui/Text';
import { Panel } from '@/components/ui/Panel';
import { ActionButton } from '@/components/ui/ActionButton';
import { usePalette } from '@/theme/usePalette';
import { useGame } from '@/features/game/store';
import { campaign, mapBasemap, submitTurn, type MapNode } from '@/features/game/api';

type ViewBox = { cx: number; cy: number; w: number };

const MIN_WIDTH = 34;
const START_WIDTH = 150;

function clamp(value: number, low: number, high: number) {
  return Math.min(high, Math.max(low, value));
}

export default function MapScreen() {
  const p = usePalette();
  const queryClient = useQueryClient();
  const { width, height } = useWindowDimensions();
  const id = useGame((s) => s.activeCampaignId);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [view, setView] = useState<ViewBox | null>(null);
  const [walking, setWalking] = useState(false);
  const initialized = useRef(false);
  const lastCurrent = useRef<string | null>(null);

  const game = useQuery({
    queryKey: ['campaign', id],
    queryFn: () => campaign(id!),
    enabled: Boolean(id),
    refetchInterval: (query) =>
      walking || query.state.data?.phase === 'resolving' ? 2000 : false,
  });
  const data = game.data;

  const basemap = useQuery({
    queryKey: ['map', data?.pack_id],
    queryFn: () => mapBasemap(data!.pack_id),
    enabled: Boolean(data?.pack_id),
    staleTime: Infinity,
    gcTime: 60 * 60 * 1000,
  });

  const mapW = basemap.data?.width || 1000;
  const mapH = basemap.data?.height || 1093;
  const nodes = useMemo<MapNode[]>(() => data?.map_nodes || [], [data?.map_nodes]);
  const current = useMemo(
    () => nodes.find((node) => node.current || node.id === data?.anchor?.location?.id) || null,
    [nodes, data?.anchor?.location?.id],
  );
  const visited = useMemo(() => nodes.filter((node) => node.visited), [nodes]);
  const selected = useMemo(
    () => nodes.find((node) => node.id === selectedId) || null,
    [nodes, selectedId],
  );
  const shown = selected || current;
  const reachable = Boolean(selected && current && selected.id !== current.id
    && current.connections.includes(selected.id));
  const neighbours = useMemo(() => {
    if (!current) return [];
    return nodes.filter((node) => current.connections.includes(node.id));
  }, [nodes, current]);

  const mapWidth = Math.max(280, width - 36);
  const mapHeight = Math.min(
    420,
    Math.max(240, Math.min(mapWidth * (mapH / mapW), height * 0.42)),
  );

  const fit = useCallback((items: MapNode[], minWidth = START_WIDTH): ViewBox => {
    const points = items.length ? items : current ? [current] : [];
    if (!points.length) return { cx: mapW / 2, cy: mapH / 2, w: mapW };
    const xs = points.map((node) => node.x);
    const ys = points.map((node) => node.y);
    const minX = Math.min(...xs);
    const maxX = Math.max(...xs);
    const minY = Math.min(...ys);
    const maxY = Math.max(...ys);
    const w = clamp(Math.max(maxX - minX, maxY - minY, minWidth) * 1.45, MIN_WIDTH, mapW);
    return { cx: (minX + maxX) / 2, cy: (minY + maxY) / 2, w };
  }, [current, mapW, mapH]);

  useEffect(() => {
    if (initialized.current || !basemap.data || !nodes.length || !current) return;
    initialized.current = true;
    const points = visited.length ? visited : [current];
    const fitted = fit(points);
    setView({ ...fitted, cx: current.x, cy: current.y });
  }, [basemap.data, nodes.length, current, visited, fit]);

  useEffect(() => {
    if (!current) return;
    if (lastCurrent.current && lastCurrent.current !== current.id) {
      setSelectedId(null);
      setView((value) => (value ? { ...value, cx: current.x, cy: current.y } : fit([current])));
      if (walking) setWalking(false);
    }
    lastCurrent.current = current.id;
  }, [current, fit, walking]);

  useEffect(() => {
    if (!walking) return;
    const timer = setTimeout(() => setWalking(false), 30000);
    return () => clearTimeout(timer);
  }, [walking]);

  const mapWidthRef = useRef(mapWidth);
  const viewRef = useRef(view);
  mapWidthRef.current = mapWidth;
  viewRef.current = view;
  const panStart = useRef<ViewBox | null>(null);
  const pan = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => false,
      onMoveShouldSetPanResponder: (_, gesture) =>
        Math.abs(gesture.dx) > 6 || Math.abs(gesture.dy) > 6,
      onPanResponderGrant: () => {
        panStart.current = viewRef.current;
      },
      onPanResponderMove: (_, gesture) => {
        const start = panStart.current;
        if (!start) return;
        const perPixel = start.w / Math.max(1, mapWidthRef.current);
        setView({
          ...start,
          cx: start.cx - gesture.dx * perPixel,
          cy: start.cy - gesture.dy * perPixel,
        });
      },
      onPanResponderRelease: () => {
        panStart.current = null;
      },
    }),
  ).current;

  const zoom = (factor: number) => {
    setView((value) => {
      const base = value || { cx: mapW / 2, cy: mapH / 2, w: mapW };
      return { ...base, w: clamp(base.w / factor, MIN_WIDTH, mapW) };
    });
  };

  const centreOnPlayer = () => {
    if (!current) return;
    setView((value) => (value ? { ...value, cx: current.x, cy: current.y } : fit([current])));
  };

  const walk = useMutation({
    mutationFn: (node: MapNode) => {
      const key = Math.random().toString(36).slice(2) + Date.now().toString(36);
      return submitTurn(id!, `Идти: ${node.name}`, key);
    },
    onSuccess: (result: any) => {
      if (result?.error) {
        Alert.alert('Не вышло', result.message || 'Попробуй ещё раз.');
        return;
      }
      if (result?.clarifying_question) {
        Alert.alert('Уточни', result.clarifying_question);
        return;
      }
      setWalking(true);
      setSelectedId(null);
      void queryClient.invalidateQueries({ queryKey: ['campaign', id] });
      void queryClient.invalidateQueries({ queryKey: ['step', id] });
      void queryClient.invalidateQueries({ queryKey: ['steps', id] });
    },
    onError: (error: any) => Alert.alert('Не вышло', error?.message || 'Попробуй ещё раз.'),
  });

  if (!id) {
    return (
      <Screen>
        <TopBar title="Карта" eyebrow="Москва" />
        <Panel>
          <AppText variant="heading">Сначала открой историю</AppText>
          <AppText variant="muted">Карта покажет Москву и места, где уже был твой герой.</AppText>
        </Panel>
      </Screen>
    );
  }

  if (basemap.isError) {
    return (
      <Screen>
        <TopBar title="Карта" eyebrow="Москва" />
        <Panel>
          <AppText variant="heading">Карта не загрузилась</AppText>
          <AppText variant="muted">Проверь соединение и попробуй ещё раз.</AppText>
          <ActionButton title="Повторить" onPress={() => void basemap.refetch()} />
        </Panel>
      </Screen>
    );
  }

  const vb = view || { cx: mapW / 2, cy: mapH / 2, w: mapW };
  const vbHeight = vb.w * (mapH / mapW);
  const scale = Math.min(mapWidth / vb.w, mapHeight / vbHeight);
  const viewX = vb.cx - vb.w / 2;
  const viewY = vb.cy - vbHeight / 2;
  const transform = `translate(${(mapWidth - vb.w * scale) / 2 - viewX * scale} ${(mapHeight - vbHeight * scale) / 2 - viewY * scale}) scale(${scale})`;
  const px = vb.w / mapWidth;
  const nodeR = Math.max(px * 5, 2);
  const labelSize = Math.max(px * 11, 4);
  const stroke = Math.max(px, 0.4);
  const here = current?.name || data?.anchor?.location?.name || 'Неизвестное место';
  const resolving = walking || data?.phase === 'resolving';
  const statusText = !shown
    ? ''
    : shown.id === current?.id
      ? 'Ты здесь'
      : reachable
        ? 'Дорога известна'
        : 'Дорога пока не найдена';

  return (
    <Screen>
      <TopBar title="Карта" eyebrow={`${here} · Москва`} />

      <View style={[styles.controls, { backgroundColor: p.surface, borderColor: p.border }]}>
        <Pressable onPress={() => zoom(1.5)} style={styles.control} accessibilityLabel="Приблизить">
          <Plus size={18} color={p.text} />
        </Pressable>
        <Pressable onPress={() => zoom(1 / 1.5)} style={styles.control} accessibilityLabel="Отдалить">
          <Minus size={18} color={p.text} />
        </Pressable>
        <Pressable onPress={centreOnPlayer} style={styles.control} accessibilityLabel="К себе">
          <Crosshair size={18} color={p.accent} />
        </Pressable>
        <Pressable onPress={() => setView({ cx: mapW / 2, cy: mapH / 2, w: mapW })} style={styles.wideControl}>
          <AppText variant="muted">Вся Москва</AppText>
        </Pressable>
      </View>

      <View
        {...pan.panHandlers}
        style={[styles.canvas, { backgroundColor: p.surface, borderColor: p.border, height: mapHeight }]}
      >
        <Svg width={mapWidth} height={mapHeight}>
          <G transform={transform}>
          <Rect
            x={viewX} y={viewY}
            width={vb.w} height={vbHeight} fill={p.surface}
          />
          {basemap.data?.rail ? (
            <Path d={basemap.data.rail} stroke={p.border} strokeWidth={stroke * 0.7}
              fill="none" opacity={0.45} />
          ) : null}
          {basemap.data?.roads?.secondary ? (
            <Path d={basemap.data.roads.secondary} stroke={p.border} strokeWidth={stroke * 0.8}
              fill="none" opacity={0.55} />
          ) : null}
          {basemap.data?.roads?.primary ? (
            <Path d={basemap.data.roads.primary} stroke={p.muted} strokeWidth={stroke * 0.9}
              fill="none" opacity={0.45} />
          ) : null}
          {basemap.data?.roads?.trunk ? (
            <Path d={basemap.data.roads.trunk} stroke={p.muted} strokeWidth={stroke * 1.5}
              fill="none" opacity={0.6} />
          ) : null}
          {basemap.data?.roads?.motorway ? (
            <Path d={basemap.data.roads.motorway} stroke={p.muted} strokeWidth={stroke * 2.1}
              fill="none" opacity={0.75} />
          ) : null}
          {basemap.data?.water ? (
            <Path d={basemap.data.water} stroke={p.accent} strokeWidth={stroke * 1.7}
              fill="none" opacity={0.55} />
          ) : null}

          {current && neighbours.map((node) => (
            <Path
              key={`road-${node.id}`}
              d={`M${current.x},${current.y}L${node.x},${node.y}`}
              stroke={p.accent} strokeWidth={stroke * 0.9} strokeDasharray={`${px * 3},${px * 3}`}
              fill="none" opacity={0.55}
            />
          ))}

          {basemap.data?.labels && vb.w > 520
            ? basemap.data.labels
              .filter((label) => label.kind === 'center'
                || ['Красная площадь', 'Арбат', 'ВДНХ', 'МГУ', 'Чертаново',
                  'Битцевский лес', 'Царицыно', 'Павелецкий вокзал'].includes(label.text))
              .map((label) => (
                <SvgText
                  key={`label-${label.text}`}
                  x={label.x} y={label.y}
                  fill={p.muted} fontSize={labelSize * 0.75} opacity={0.5}
                  textAnchor="middle"
                >
                  {label.text}
                </SvgText>
              ))
            : null}

          {nodes.map((node) => {
            const isCurrent = node.id === current?.id;
            const isSelected = node.id === selectedId;
            const isNeighbour = Boolean(current?.connections.includes(node.id));
            const fill = isCurrent ? p.accent : isSelected ? p.gold : node.visited ? p.accentSoft : p.surface;
            const line = isCurrent ? p.text : isSelected ? p.gold : isNeighbour ? p.accent : p.muted;
            return (
              <G key={node.id} onPress={() => setSelectedId(node.id)}>
                <Circle cx={node.x} cy={node.y} r={nodeR * 3.2} fill="transparent" />
                <Circle
                  cx={node.x} cy={node.y} r={nodeR}
                  fill={fill} stroke={line} strokeWidth={stroke * (isCurrent ? 2 : 1.3)}
                />
                {isCurrent ? (
                  <Circle cx={node.x} cy={node.y} r={nodeR * 2.1} fill="none"
                    stroke={p.accent} strokeWidth={stroke} opacity={0.5} />
                ) : null}
              </G>
            );
          })}

          {nodes.filter((node) =>
            node.id === current?.id || node.id === selectedId
            || node.visited || vb.w < 420,
          ).map((node) => (
            <SvgText
              key={`name-${node.id}`}
              x={node.x + nodeR * 6} y={node.y + nodeR * 1.6}
              fill={node.id === current?.id ? p.text : p.muted}
              fontSize={labelSize} textAnchor="start"
              fontWeight={node.id === current?.id ? '700' : '400'}
            >
              {node.name}
            </SvgText>
          ))}
          </G>
        </Svg>

        {!basemap.data ? (
          <View pointerEvents="none" style={styles.loading}>
            <AppText variant="muted">Загружаю карту Москвы…</AppText>
          </View>
        ) : null}

        {resolving ? (
          <View pointerEvents="none" style={[styles.banner, { backgroundColor: p.accentSoft, borderColor: p.accent }]}>
            <AppText variant="muted">Мир описывает дорогу…</AppText>
          </View>
        ) : null}
      </View>

      <View style={styles.legend}>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, { backgroundColor: p.accent }]} />
          <AppText variant="muted">ты</AppText>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, { backgroundColor: p.accentSoft, borderColor: p.accent }]} />
          <AppText variant="muted">был здесь</AppText>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.legendDot, { backgroundColor: p.surface, borderColor: p.accent }]} />
          <AppText variant="muted">дорога известна</AppText>
        </View>
      </View>

      <Panel>
        <AppText variant="heading">{shown ? shown.name : 'Москва'}</AppText>
        {shown ? (
          <>
            <AppText variant="muted">{shown.district || 'Москва'}</AppText>
            <AppText variant="muted">
              {shown.biome} · {shown.danger}
            </AppText>
            {typeof shown.lat === 'number' && typeof shown.lon === 'number' ? (
              <AppText variant="muted">
                {shown.lat.toFixed(4)}° с. ш., {shown.lon.toFixed(4)}° в. д.
              </AppText>
            ) : null}
            <AppText variant="muted">{statusText}</AppText>
            {reachable ? (
              <ActionButton
                title={walk.isPending ? 'Идём…' : 'Идти сюда'}
                onPress={() => walk.mutate(shown)}
              />
            ) : null}
          </>
        ) : (
          <AppText variant="muted">Коснись точки, чтобы узнать подробности.</AppText>
        )}
        <AppText variant="muted">
          Открыто мест: {visited.length} · известно дорог: {nodes.length - visited.length}
        </AppText>
      </Panel>

      {neighbours.length > 0 ? (
        <Panel>
          <AppText variant="label" style={{ color: p.accent }}>ОТСЮДА МОЖНО ПОЙТИ</AppText>
          <View>
            {neighbours.map((node) => (
              <Pressable
                key={node.id}
                onPress={() => setSelectedId(node.id)}
                style={styles.row}
              >
                <View style={{ flex: 1 }}>
                  <AppText>{node.name}</AppText>
                  <AppText variant="muted">{node.district || 'Москва'}</AppText>
                </View>
                <AppText variant="muted">{node.visited ? 'известно' : 'дорога'} →</AppText>
              </Pressable>
            ))}
          </View>
        </Panel>
      ) : null}

      <AppText variant="muted" style={styles.attribution}>
        Карта: {basemap.data?.attribution || '© OpenStreetMap contributors'}
      </AppText>
    </Screen>
  );
}

const styles = StyleSheet.create({
  controls: {
    flexDirection: 'row', alignItems: 'center', gap: 4, borderWidth: 1,
    borderRadius: 14, padding: 4,
  },
  control: { width: 38, height: 34, alignItems: 'center', justifyContent: 'center', borderRadius: 10 },
  wideControl: { marginLeft: 'auto', paddingHorizontal: 12, height: 34, justifyContent: 'center' },
  canvas: { borderWidth: 1, borderRadius: 18, overflow: 'hidden', position: 'relative' },
  loading: {
    ...StyleSheet.absoluteFillObject, alignItems: 'center', justifyContent: 'center',
  },
  banner: {
    position: 'absolute', left: 10, right: 10, bottom: 10, borderWidth: 1,
    borderRadius: 12, paddingHorizontal: 12, paddingVertical: 8,
  },
  legend: { flexDirection: 'row', flexWrap: 'wrap', gap: 14, paddingHorizontal: 2 },
  legendItem: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  legendDot: { width: 10, height: 10, borderRadius: 5, borderWidth: 1, borderColor: 'transparent' },
  row: {
    flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between',
    paddingVertical: 9, gap: 10,
  },
  attribution: { textAlign: 'center', opacity: 0.75 },
});
