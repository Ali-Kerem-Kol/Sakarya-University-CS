# DFA (Deterministic Finite Automaton) sınıfı tanımı
class DFA:
    def __init__(self, states, alphabet, transition_function, start_state, accept_states):
        """
        DFA'nın özelliklerini başlatır.
        
        Parametreler:
        states: DFA'nın durumlarını içeren küme (örneğin {"q0", "q1", "q2"})
        alphabet: DFA'nın kullandığı alfabeyi içeren küme (örneğin {"0", "1"})
        transition_function: Durumlar ve semboller arasındaki geçişleri tanımlar
        start_state: DFA'nın başlangıç durumu
        accept_states: Kabul durumlarını içeren küme
        """
        self.states = states  # DFA'nın durumlarını saklar.
        self.alphabet = alphabet  # Kullanılan alfabeyi saklar.
        self.transition_function = transition_function  # Geçiş fonksiyonunu saklar.
        self.start_state = start_state  # Başlangıç durumunu saklar.
        self.accept_states = accept_states  # Kabul durumlarını saklar.

    def remove_unreachable_states(self):
        """
        DFA'daki ulaşılmayan durumları kaldırır.
        """
        reachable = set()  # Ulaşılabilir durumları saklayan küme.
        stack = [self.start_state]  # Başlangıç durumundan başlar.

        # DFS (Depth First Search) algoritması ile ulaşılabilir durumları bul.
        while stack:
            state = stack.pop()  # Yığından bir durum al.
            if state not in reachable:  # Eğer durum daha önce ziyaret edilmediyse:
                reachable.add(state)  # Durumu ulaşılabilir olarak işaretle.
                # Her sembol için geçiş yapılan durumları kontrol et.
                for symbol in self.alphabet:
                    next_state = self.transition_function.get((state, symbol))
                    if next_state and next_state not in reachable:
                        stack.append(next_state)  # Yeni durumu yığına ekle.

        # DFA'nın durumlarını, geçişlerini ve kabul durumlarını güncelle.
        self.states = reachable  # Sadece ulaşılabilir durumları sakla.
        self.transition_function = {
            (state, symbol): next_state
            for (state, symbol), next_state in self.transition_function.items()
            if state in reachable and next_state in reachable
        }
        self.accept_states = {state for state in self.accept_states if state in reachable}

    def minimize(self):
        """
        DFA'yı minimize eder. Gereksiz durumları birleştirir veya kaldırır.
        """
        self.remove_unreachable_states()  # Öncelikle ulaşılmayan durumları kaldır.

        # 1. Adım: Kabul ve reddetme durumlarını farklı gruplara ayır.
        partition = [set(self.accept_states), self.states - set(self.accept_states)]
        if not partition[1]:  # Eğer reddeden durumlar yoksa, bu grubu kaldır.
            partition.pop()

        def get_partition(state):
            """
            Verilen bir durumun hangi gruba ait olduğunu bulur.
            """
            for i, group in enumerate(partition):
                if state in group:
                    return i

        # 2. Adım: Grupları ayrıştırma işlemini tekrar et.
        changed = True
        while changed:
            changed = False
            new_partition = []  # Yeni oluşturulan gruplar burada tutulur.

            for group in partition:
                subgroups = {}  # Alt gruplar için bir sözlük.
                for state in group:
                    # Her durum için, geçiş imzasını oluştur.
                    signature = tuple(
                        (symbol, get_partition(self.transition_function.get((state, symbol), -1)))
                        for symbol in self.alphabet
                    )
                    subgroups.setdefault(signature, set()).add(state)

                new_partition.extend(subgroups.values())  # Alt grupları yeni partisyona ekle.
                if len(subgroups) > 1:  # Eğer grup ayrıldıysa:
                    changed = True

            partition = new_partition  # Yeni gruplar, eski grupların yerini alır.

        # 3. Adım: Her grup için bir temsilci durum seç ve geçiş fonksiyonunu güncelle.
        state_map = {}
        for group in partition:
            representative = next(iter(group))  # İlk durumu grup temsilcisi seç.
            for state in group:
                state_map[state] = representative

        # DFA'yı güncelle:
        self.states = {next(iter(group)) for group in partition}
        self.transition_function = {
            (state_map[state], symbol): state_map[next_state]
            for (state, symbol), next_state in self.transition_function.items()
            if state in state_map and next_state in state_map
        }
        self.start_state = state_map[self.start_state]  # Başlangıç durumu.
        self.accept_states = {state_map[state] for state in self.accept_states}  # Kabul durumları.

    def __repr__(self):
        """
        DFA'nın okunabilir bir metinsel temsilini döndürür.
        """
        return (
            f"States: {self.states}\n"
            f"Alphabet: {self.alphabet}\n"
            f"Start State: {self.start_state}\n"
            f"Accept States: {self.accept_states}\n"
            f"Transitions: {self.transition_function}\n"
        )

# Örnek bir DFA tanımlaması.
dfa = DFA(
    states={"q0", "q1", "q2", "q3"},  # Durumlar.
    alphabet={"0", "1"},  # Alfabe.
    transition_function={  # Geçiş fonksiyonları.
        ("q0", "0"): "q1",
        ("q0", "1"): "q0",
        ("q1", "0"): "q0",
        ("q1", "1"): "q1",
        ("q2", "0"): "q3",
        ("q2", "1"): "q2",
        ("q3", "0"): "q2",
        ("q3", "1"): "q3",
    },
    start_state="q0",  # Başlangıç durumu.
    accept_states={"q0"},  # Kabul durumları.
)

# DFA'nın minimizasyon öncesi hali.
print("DFA Önce:\n", dfa)

# DFA'yı minimize et.
dfa.minimize()

# DFA'nın minimizasyon sonrası hali.
print("DFA Sonra:\n", dfa)
